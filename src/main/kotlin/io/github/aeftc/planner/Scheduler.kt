package io.github.aeftc.planner

import io.github.aeftc.planner.logging.LogProvider
import kotlin.math.ceil
import kotlin.math.max


class Scheduler
@JvmOverloads constructor(private val throwDebuggingErrors: Boolean = false) {
    private val log = LogProvider.instance.getLogger("Scheduler")

    private val locks: MutableMap<String, Int?> = mutableMapOf()
    private val tasks: MutableMap<Int, Task> = mutableMapOf()
    private val lockIdName: MutableMap<String, SharedResource> = mutableMapOf()

    companion object {
        const val EVICT = true
        const val ROLLING_AVERAGE_SIZE = 10000

        private fun <T : Comparable<T>, N : List<T>> percentile(k: N, l: Double): T {
            val index = ceil(l * k.size).toInt()
            return k[max(0, index - 1)]
        }

        internal fun getCaller(): String {
            try {
                throw Exception()
            } catch (e: Exception) {
                val stack = e.stackTrace
                for (frame in stack) {
                    if (frame.className.contains("dev.aether.collaborative_multitasking")) continue
                    return "${
                        frame.className.split(".").last()
                    }.${frame.methodName} line ${frame.lineNumber}"
                }
            }
            return "<unknown source>"
        }
    }

    val tickTimes: MutableList<Double> = mutableListOf()

    var nextId: Int = 0
        private set
    private var tickCount = 0

    private fun selectState(state: Task.State): List<Task> {
        return tasks.values.filter { it.state == state }
    }

    private fun allFreed(requirements: Set<SharedResource>): Boolean {
        return requirements.all { locks[it.id] == null }
    }

    private fun tickMarkStartable() {
        selectState(Task.State.NotStarted)
            .filter {
                it.invokeCanStart()
            }
            .forEach {
                if (allFreed(it.requirements())) {
                    it.setState(Task.State.Starting)
                    // acquire locks
                    for (lock in it.requirements()) {
                        log.d("$it acquired $lock")
                        if (locks[lock.id] != null) {
                            log.w("we're gonna make ${tasks[locks[lock.id]]} crash when it finishes due to mis-acquire")
                        }
                        locks[lock.id] = it.myId
                        lockIdName[lock.id] = lock
                    }
                }
            }
    }

    private fun tickStartMarked() {
        selectState(Task.State.Starting)
            .forEach {
                try {
                    it.invokeOnStart()
                    if (it.invokeIsCompleted()) {
                        it.setState(Task.State.Finishing)
                    } else {
                        it.setState(Task.State.Ticking)
                    }
                } catch (e: Exception) {
                    log.e(
                        String.format(
                            "Error while marking %s to start:",
                            it.toString()
                        ), e
                    )
                }
            }
    }

    private fun tickTick() {
        selectState(Task.State.Ticking)
            .forEach {
                try {
                    it.invokeOnTick()
                    if (it.invokeIsCompleted()) it.setState(Task.State.Finishing)
                } catch (e: Exception) {
                    log.e(String.format("Error while ticking %s:", it.toString()), e)
                }
            }
    }

    private fun tickFinish() {
        val candidates = selectState(Task.State.Finishing)
        candidates.forEach(::release)
    }

    private fun release(task: Task, cancel: Boolean = false) {
        val targetState = if (cancel) Task.State.Cancelled else Task.State.Finished
        if (task.state == Task.State.NotStarted) {
            task.setState(targetState)
            return
        }
        try {
            task.invokeOnFinish()
        } catch (e: Exception) {
            log.e(
                String.format(
                    "Error while processing %s finish handler:",
                    task.toString()
                ), e
            )
        }
        task.setState(targetState)
        for (lock in task.requirements()) {
            if (locks[lock.id] != task.myId) {
                if (throwDebuggingErrors)
                    throw IllegalStateException("$task (which just finished) does not own lock $lock that it is supposed to own")
                else log.e("$task (which just finished) does not own lock $lock that it is supposed to own")
            }
            locks[lock.id] = null
            log.d("$task released $lock")
        }
    }

    fun tick() {
        val start = System.nanoTime()
        tickMarkStartable()
        tickStartMarked()
        tickTick()
        tickFinish()
        tickCount++
        val durat = (System.nanoTime() - start) / 1000000.0
        tickTimes.add(durat)
        if (durat > 1000) {
            log.e(String.format("Warning: tick %d took %.2f ms", tickCount - 1, durat))
        }
        if (EVICT && tickTimes.size > ROLLING_AVERAGE_SIZE) tickTimes.removeAt(0)
    }

    /**
     * We need better profiling. Maybe draw a cool flame chart?
     * [!] writing to lists takes lots of time, especially in performance-critical ticks
     */
    fun statsheet(): String {
        var waiting = 0
        var progress = 0
        var done = 0
        var cancelled = 0
        for (task in tasks.values) {
            when (task.state) {
                Task.State.NotStarted -> waiting++
                Task.State.Finished -> done++
                Task.State.Cancelled -> cancelled++
                else -> progress++
            }
        }

        val s = tickTimes.sorted()
        return String.format(
            "${tasks.size} tasks: $waiting waiting, $progress running, $done done, $cancelled cancel\n" +
                    "%d samples:\n" +
                    "  [Min  ][1%%   ][5%%   ][32%%  ][50%%  ][68%%  ][95%%  ][99%%  ][Max  ]\n" +
                    "  %7.1f%7.1f%7.1f%7.1f%7.1f%7.1f%7.1f%7.1f%7.1f",
            s.size,
            percentile(s, 0.0),
            percentile(s, 0.01),
            percentile(s, 0.05),
            percentile(s, 0.32),
            percentile(s, 0.5),
            percentile(s, 0.68),
            percentile(s, 0.95),
            percentile(s, 0.99),
            percentile(s, 1.0)
        )
    }

    fun getTicks(): Int {
        return tickCount
    }

    @Suppress("unused") // api
    fun task(configure: Task.() -> Unit): Task {
        val task = Task(this)
        task.configure()
        task.name = getCaller()
        task.register()
        return task
    }

    fun register(task: Task): Int {
        val id = nextId++
        tasks[id] = task
        for (lock in task.requirements()) {
            if (!locks.containsKey(lock.id)) {
                locks[lock.id] = null
            }
        }
        return id
    }

    @Suppress("unused")
    fun isResourceInUse(resource: SharedResource): Boolean {
        return locks[resource.id] != null
    }

    @Suppress("unused")
    fun panic() {
        for (task in tasks.values) {
            if (task.state == Task.State.Finished || task.state == Task.State.NotStarted || task.state == Task.State.Cancelled) continue
            task.invokeOnFinish()
            task.setState(Task.State.Finished)
        }
    }

    fun hasJobs(): Boolean {
        return tasks.values.any { it.state != Task.State.Finished && !it.daemon }
    }

    fun runToCompletion(ok: () -> Boolean) {
        while (hasJobs() && ok()) {
            tick()
        }
    }

    /**
     * Stops any tasks matching the predicate that are not already finished or haven't started yet.
     * Resources owned by matching tasks are guaranteed to be released after this call.
     */

    fun filteredStop(
        predicate: (Task) -> Boolean,
        cancel: Boolean,
        dropNonStarted: Boolean
    ) {
        tasks.values
            .filter { it.state != Task.State.Finished && it.state != Task.State.NotStarted && it.state != Task.State.Cancelled }
            .filter(predicate)
            .forEach {
                release(it, cancel)
            }
        if (dropNonStarted) {
            val dropped = tasks.filterInPlace { _, v ->
                v.state == Task.State.NotStarted && predicate(v)
            }
            log.i("dropped ${dropped.size} tasks: ${dropped.joinToString(", ")}")
        }
    }

    fun filteredStop(predicate: (Task) -> Boolean, cancel: Boolean) = filteredStop(
        predicate,
        cancel = cancel, dropNonStarted = false
    )

    fun filteredStop(predicate: (Task) -> Boolean) = filteredStop(
        predicate,
        cancel = true,
        dropNonStarted = false
    )
}
