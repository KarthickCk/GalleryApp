package com.app.gallery.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.MediaStore
import androidx.compose.foundation.content.MediaType
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import com.app.gallery.data.extension.queryFlow
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GalleryRepository @Inject constructor(
    private val contentResolver: ContentResolver,
) : IGalleryRepository {

    private val projection = arrayOf(
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

        val queryArgs = Bundle().apply {
            putAll(
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to "(media_type = 1) OR (media_type = 3)",
                )
            )
        }
        val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

        return contentResolver.queryFlow(
            uri,
            projection,
            queryArgs
        ).mapToAlbum()
            .map {
                Result.success(it)
            }
            .catch {
                Result.failure<List<Album>>(it)
            }
    }

    private fun Flow<Cursor?>.mapToAlbum(): Flow<List<Album>> {
       return map { cursor ->
            cursor.let {
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
}

fun Cursor?.tryGetString(columnIndex: Int, fallback: String? = null): String? {
    return this?.getStringOrNull(columnIndex) ?: fallback
}