package com.letsclimbthis.esigtesttask.ui.utils

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.letsclimbthis.esigtesttask.log
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.String.valueOf
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*

fun View.collapse() {
    val initialHeight = measuredHeight
    val anim: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height =
                    initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }
        override fun willChangeBounds() = true
    }
    anim.duration = (initialHeight / context.resources.displayMetrics.density).toInt().toLong()
    startAnimation(anim)
}

fun View.expand() {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetedHeight = measuredHeight
    layoutParams.height = 0
    visibility = View.VISIBLE
    val anim: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT
                else (targetedHeight * interpolatedTime).toInt()
            requestLayout()
        }
        override fun willChangeBounds() = true
    }
    anim.duration = (targetedHeight / context.resources.displayMetrics.density).toInt().toLong()
    startAnimation(anim)
}

fun File.getExtension() = MimeTypeMap.getFileExtensionFromUrl(toString())

fun File.getSize() = run {
    "${valueOf(length() / 1024)} KB"
}

fun Fragment.displayMessage(message: String) {
    view?.let {
        Snackbar
            .make(it, message, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }
}

fun X509Certificate.getCN(): String {
    val s = subjectDN.name.substring(subjectDN.name.indexOf("CN="))
    return s.substring(3 until s.indexOf(","))
}

fun Long.millisecondsToDate(): String {
    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm", Locale.getDefault(Locale.Category.FORMAT))
    return simpleDateFormat.format(this)
}

