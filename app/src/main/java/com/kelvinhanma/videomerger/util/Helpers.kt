package com.kelvinhanma.videomerger.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a epoch in Long to human readable output.
 */
fun Long.toHumanReadableString(): String {
    return SimpleDateFormat().format(
        Date(this)
    )
}

/**
 * Helper to inflate an layout.
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}