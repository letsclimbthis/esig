package com.letsclimbthis.esigtesttask

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

var logMessage = ""

private var initialLogMessage = "------ LAST LOG -----\n" + SimpleDateFormat(
    "dd.MM.yyyy 'at' HH:mm:ss",
    Locale.getDefault(Locale.Category.FORMAT)
).format(System.currentTimeMillis())

fun log(msg: String) {
    logMessage = initialLogMessage
    logMessage = "$logMessage\n$msg"
    Log.d("mytag", msg)
}

fun log(e: Exception) {
    val st = e.stackTrace.fold("") { prev, ste -> "$prev\n$ste" }
    logMessage = "$logMessage\n$st"
    Log.d("mytag", st)

}

fun log(msg: String, e: Exception) {
    val st = e.stackTrace.fold("") { prev, ste -> "$prev\n$ste" }
    val s = "$msg\n$st"
    logMessage = "$logMessage\n$s"
    Log.d("mytag", s)
}