package io.github.aeftc.planner.logging

import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormat

class BasicLog(private val dateFormat: DateTimeFormat<LocalDateTime> = DEFAULT_DATETIME_FORMAT) : LogProvider {
    companion object {
        val DEFAULT_DATETIME_FORMAT = LocalDateTime.Format {
            hour()
            chars(":")
            minute()
            chars(":")
            second()
        }
    }

    private fun buildLogString(level: String, tag: String, message: String): String {
        val now = Clock.System.now()
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateString = localNow.format(dateFormat)
        val headers = String.format("[%s] %s <%s> ", dateString, level, tag)
        val merged = StringBuilder(headers)
        message.split("\n").forEachIndexed { idx, line ->
            if (idx != 0) {
                // indent the next lines so that they line up with the header
                merged.append("\n").append(" ".repeat(headers.length))
            }
            merged.append(line)
        }
        return merged.toString()
    }

    private fun log(level: String, tag: String, message: String) {
        println(buildLogString(level, tag, message))
    }

    override fun d(tag: String, message: String) = log("DEBUG", tag, message)

    override fun i(tag: String, message: String) = log("INFO", tag, message)

    override fun w(tag: String, message: String) = log("WARN", tag, message)

    override fun e(tag: String, message: String) = log("ERROR", tag, message)

    override fun e(tag: String, message: String, throwable: Throwable) {
        log(
            "ERROR", tag,
            (message + "\n" + throwable::class.simpleName + ": " + (throwable.message
                ?: "(No message provided)") + "\n" + throwable.stackTraceToString())
        )
    }

    override fun f(tag: String, message: String) = log("FATAL", tag, message)
}