package io.github.aeftc.planner.logging

class PartialLog(private val logProvider: LogProvider, private val tag: String) {
    fun d(message: String) = logProvider.d(tag, message)
    fun i(message: String) = logProvider.i(tag, message)
    fun w(message: String) = logProvider.w(tag, message)
    fun e(message: String) = logProvider.e(tag, message)
    fun e(message: String, throwable: Throwable) = logProvider.e(tag, message, throwable)
    fun f(message: String) = logProvider.f(tag, message)
    fun c(message: String) = logProvider.c(tag, message)
}