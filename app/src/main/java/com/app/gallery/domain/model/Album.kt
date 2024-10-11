package com.app.gallery.domain.model

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class Album(
    val id: Long = 0,
    val label: String,
    var uri: Uri,
    var pathToThumbnail: String,
    var relativePath: String,
    var count: Long = 0,
) : Parcelable {
     companion object {
         const val ALL_IMAGE_ID = -1L
         const val ALL_VIDEO_ID = -2L
     }
}