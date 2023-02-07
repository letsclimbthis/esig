package com.letsclimbthis.esigtesttask

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

var logMessage = "------ LAST LOG -----\n" + SimpleDateFormat(
    "dd.MM.yyyy 'at' HH:mm:ss",
    Locale.getDefault(Locale.Category.FORMAT)
).format(System.currentTimeMillis())

fun log(msg: String) {
    Log.d("mytag", msg)
    logMessage = "$logMessage\n$msg"
}

fun log(e: Exception) {
    Log.d("mytag", e.toString())
    logMessage = "$logMessage\n$e"
}