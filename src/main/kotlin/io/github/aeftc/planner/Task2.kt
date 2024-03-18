package io.github.aeftc.planner

interface ActionableTask {
    enum class State {
        // Normal state progression
        /**
         * Task has not yet started. shouldStart() is called every tick.
         * (in Classic Tasks, this is named NOT_STARTED)
         * @see Task.State.NotStarted
         */
        Waiting,

        /**
         * onStart and isComplete called here; if isComplete is true
         * then the task will immediately finish
         */
        Starting,

        /**
         * Task is ticking normally. tick() and isComplete() called every tick.
         */
        Running,

        /**
         * onFinish called here - if a task is cancel()d from here, onFinish will not be called again
         */
        Finishing,

        /**
         * Task is done and can be discarded.
         */
        Finished,

        // Pausing
        /**
         * Pause callbacks in progress.
         */
        Pausing,

        /**
         * Waiting to resume. Cancellation will result in the cancelled state.
         */
        Paused,

        /**
         * Resume callbacks in progress.
         */
        Resuming,

        // Cancellation
        /**
         * Task was cancelled while running or paused.
         */
        Cancelled,

        /**
         * Task was cancelled while waiting to start.
         */
        Dropped,
    }

    fun shouldStart(): Boolean
    fun start()
    fun step()
    fun shouldFinish(): Boolean
    fun finish()

    fun pause()
    fun resume()
    fun cancel()
}