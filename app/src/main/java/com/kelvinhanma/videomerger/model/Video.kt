package com.kelvinhanma.videomerger.model

import android.graphics.Bitmap

data class Video(val id: Long, val name: String, val dateTaken: Long, val duration: Int, val preview: Bitmap?)