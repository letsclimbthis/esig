package com.letsclimbthis.esigtesttask.ui.showlastcrash

import android.content.Context
import com.letsclimbthis.esigtesttask.logMessage
import java.text.SimpleDateFormat
import java.util.*

class UnexpectedCrashSaver(val context: Context) : Thread.UncaughtExceptionHandler  {

    private val defaultUEH: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(p0: Thread, p1: Throwable) {

        val timeStamp = SimpleDateFormat(
            "dd.MM.yyyy 'at' HH:mm:ss",
            Locale.getDefault(Locale.Category.FORMAT)
        ).format(System.currentTimeMillis())

        val stackTrace = p1.stackTrace
            .fold(
                "\"------ LAST EXCEPTION ------\n\n$timeStamp\nException: $p1\n\n" +
                        "-- Stack trace --\n"
            ) {
                    prev, ste -> "$prev\n$ste"
            }

        val cause = p1.cause?.let {
            stackTrace
                .fold(
                    "\n\nCause: $p1\n\n-- Cause --\n"
                ) {
                        prev, ste -> "$prev$ste"
                }
        }

        val report =
                stackTrace +
                "\n-------------------------------\n" +
                (cause?:"") +
                "\n-------------------------------\n"

        context
            .openFileOutput("stack.trace", Context.MODE_PRIVATE)
            .use {
                it.write(report.toByteArray())
            }

        defaultUEH?.uncaughtException(p0, p1)
    }
}