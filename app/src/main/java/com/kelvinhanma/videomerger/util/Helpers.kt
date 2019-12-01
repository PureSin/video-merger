package com.kelvinhanma.videomerger.util

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