package com.letsclimbthis.esigtesttask.ui.utils

import android.os.Environment

object Constants {
//    val rootDirectory = "/storage/emulated/0"
    val rootDirectory: String = Environment.getExternalStorageDirectory().absolutePath
}