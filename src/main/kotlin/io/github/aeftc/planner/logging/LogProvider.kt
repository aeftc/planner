package io.github.aeftc.planner.logging

/**
 * Generic logging provider.
 * Implement this interface to provide logging for the planner. (ex. Android Logcat, System.out, etc.)
 */
@Suppress("unused")  // API
interface LogProvider {
    companion object {
        /**
         * Which log provider is currently in use. Assign to use a different provider.
         */
        var instance: LogProvider = BasicLog()
    }

    fun getLogger(tag: String): PartialLog = PartialLog(this, tag)

    /**
     * DEBUG message.
     */
    fun d(tag: String, message: String)

    /**
     * INFO message.
     */
    fun i(tag: String, message: String)

    /**
     * WARNING message.
     */
    fun w(tag: String, message: String)

    /**
     * ERROR message without throwable.
     */
    fun e(tag: String, message: String)

    /**
     * ERROR message with throwable.
     */
    fun e(tag: String, message: String, throwable: Throwable)

    /**
     * FATAL message.
     */
    fun f(tag: String, message: String)
    fun c(tag: String, message: String) = f(tag, message)
}