@file:Suppress("unused")

package io.github.aeftc.planner.extensions

import io.github.aeftc.planner.Runnable
import io.github.aeftc.planner.Task
import io.github.aeftc.planner.TaskAction1
import io.github.aeftc.planner.TaskAction2
import io.github.aeftc.planner.TaskQuery1
import io.github.aeftc.planner.TaskQuery2

fun Task.extendOnStart(other: Runnable) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other()
    }
}
fun Task.extendOnStart(other: TaskAction1) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other(a)
    }
}
fun Task.extendOnStart(other: TaskAction2) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other(a, b)
    }
}

fun Task.isCompletedAnd(other: () -> Boolean) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other()
    }
}
fun Task.isCompletedAnd(other: TaskQuery1<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other(a)
    }
}
fun Task.isCompletedAnd(other: TaskQuery2<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other(a, b)
    }
}

fun Task.isCompletedOr(other: () -> Boolean) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other()
    }
}
fun Task.isCompletedOr(other: TaskQuery1<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.isCompletedOr(other: TaskQuery2<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other(a, b)
    }
}

fun Task.canStartAnd(other: () -> Boolean) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other()
    }
}
fun Task.canStartAnd(other: TaskQuery1<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.canStartAnd(other: TaskQuery2<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a, b)
    }
}

fun Task.canStartOr(other: () -> Boolean) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other()
    }
}
fun Task.canStartOr(other: TaskQuery1<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.canStartOr(other: TaskQuery2<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a, b)
    }
}
