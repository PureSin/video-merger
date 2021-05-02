package com.kelvinhanma.videomerger.model

import android.net.Uri

data class Video(
    val id: Long,
    val name: String,
    val dateTaken: Long,
    val duration: Int,
    val uri: Uri,
    val filePath: String
)