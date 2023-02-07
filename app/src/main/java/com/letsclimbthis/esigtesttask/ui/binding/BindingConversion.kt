package com.letsclimbthis.esigtesttask.ui.binding

import androidx.databinding.BindingConversion

@BindingConversion
fun booleanToString(bool: Boolean?) = run {
    bool?.let { if (it) "Directory" else "File" } ?: "-"
}
