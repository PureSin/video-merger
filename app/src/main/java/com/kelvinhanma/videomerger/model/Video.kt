package com.kelvinhanma.videomerger.model

// TODO  Video probably should just hold the uri
data class Video(
    val id: Long,
    val name: String,
    val dateTaken: Long,
    val duration: Int,
    val filePath: String
)