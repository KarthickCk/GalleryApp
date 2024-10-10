package com.app.gallery.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GalleryRepository @Inject constructor(
    private val contentResolver: ContentResolver,
) : IGalleryRepository {

    private val AlbumsProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.RELATIVE_PATH,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.BUCKET_ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MIME_TYPE,
    )

    override fun getAlbums(): Flow<Result<List<Album>>> {

        val images = getImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val videos = getImages(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        val list = mutableListOf<Album>()
            .apply {
                addAll(images)
                addAll(videos)
            }
        return flow {
            emit(Result.success(list))
        }
    }

    private fun getImages(uri: Uri): List<Album> {
        val cursor = contentResolver.query(
            uri, AlbumsProjection, null,
            null,
            null
        )

        return cursor.let {
            mutableMapOf<Int, Album>().apply {
                it?.use {
                    val idIndex = it.getColumnIndex(MediaStore.Files.FileColumns._ID)
                    val albumIdIndex = it.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_ID)
                    val labelIndex =
                        it.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                    val thumbnailPathIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val thumbnailRelativePathIndex =
                        it.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                    val thumbnailDateIndex =
                        it.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    val sizeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val mimeTypeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

                    if (!it.moveToFirst()) {
                        return@use
                    }

                    while (!it.isAfterLast) {
                        val bucketId = it.getInt(albumIdIndex)

                        this[bucketId]?.also { album ->
                            album.count += 1
                        } ?: run {
                            val albumId = it.getLong(albumIdIndex)
                            val id = it.getLong(idIndex)
                            val label = it.tryGetString(labelIndex, Build.MODEL)
                            val thumbnailPath = it.getString(thumbnailPathIndex)
                            val thumbnailRelativePath = it.getString(thumbnailRelativePathIndex)
                            val thumbnailDate = it.getLong(thumbnailDateIndex)
                            val size = it.getLong(sizeIndex)
                            val mimeType = it.getString(mimeTypeIndex)
                            val contentUri = if (mimeType.contains("image"))
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            else
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                            this[bucketId] = Album(
                                id = albumId,
                                label = label ?: Build.MODEL,
                                uri = ContentUris.withAppendedId(contentUri, id),
                                pathToThumbnail = thumbnailPath,
                                relativePath = thumbnailRelativePath,
                                timestamp = thumbnailDate
                            ).apply {
                                this.count += 1
                            }
                        }

                        it.moveToNext()
                    }
                }
            }.values.toList()
        }
    }
}

fun Cursor?.tryGetString(columnIndex: Int, fallback: String? = null): String? {
    return this?.getStringOrNull(columnIndex) ?: fallback
}