package com.app.gallery.domain.model

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcelable
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.random.Random

@Immutable
@Parcelize
data class Media(
    val id: Long = 0,
    val label: String,
    val uri: Uri,
    val path: String,
    val relativePath: String,
    val albumID: Long,
    val albumLabel: String,
    val mimeType: String,
) : Parcelable {

    @Stable
    override fun toString(): String {
        return "$id, $path, $mimeType"
    }

    companion object {
        fun createFromUri(context: Context, uri: Uri): Media? {
            if (uri.path == null) return null
            val extension = uri.toString().substringAfterLast(".")
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
            var duration: String? = null
            try {
                val retriever = MediaMetadataRetriever().apply {
                    setDataSource(context, uri)
                }
                val hasVideo =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                val isVideo = "yes" == hasVideo
                if (isVideo) {
                    duration =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                }
                if (mimeType.isEmpty()) {
                    mimeType = if (isVideo) "video/*" else "image/*"
                }
            } catch (_: Exception) {
            }
            var timestamp = 0L
            uri.path?.let { File(it) }?.let {
                timestamp = try {
                    it.lastModified()
                } catch (_: Exception) {
                    0L
                }
            }
            return Media(
                id = Random(System.currentTimeMillis()).nextLong(-1000, 25600000),
                label = uri.toString().substringAfterLast("/"),
                uri = uri,
                path = uri.path.toString(),
                relativePath = uri.path.toString().substringBeforeLast("/"),
                albumID = -99L,
                albumLabel = "",
                mimeType = mimeType,
            )
        }
    }
}

val Media.isRaw: Boolean get() =
    mimeType.isNotBlank() && (mimeType.startsWith("image/x-") || mimeType.startsWith("image/vnd."))

val Media.fileExtension: String get() = label.substringAfterLast(".").removePrefix(".")

val Media.volume: String get() = path.substringBeforeLast("/").removeSuffix(relativePath.removeSuffix("/"))

val Media.readUriOnly: Boolean get() = albumID == -99L && albumLabel == ""

val Media.isVideo: Boolean get() = mimeType.startsWith("video/")

val Media.isImage: Boolean get() = mimeType.startsWith("image/")