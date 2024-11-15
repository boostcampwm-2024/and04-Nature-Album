package com.and04.naturealbum.data.dto

import java.time.LocalDateTime

data class FirebaseLabel(
    val backgroundColor: String,
    val thumbnail: String
)

data class FirebasePhotoInfo(
    val uri: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val datetime: LocalDateTime
)