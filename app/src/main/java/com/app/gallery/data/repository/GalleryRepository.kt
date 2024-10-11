package com.app.gallery.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import com.app.gallery.data.DataConstants
import com.app.gallery.data.extension.mapEachRow
import com.app.gallery.data.extension.queryFlow
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.model.Media
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GalleryRepository @Inject constructor(
    private val contentResolver: ContentResolver,
) : IGalleryRepository {

    override fun getAlbums(): Flow<Result<List<Album>>> {

        val queryArgs = Bundle().apply {
            putAll(
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to "(media_type = 1) OR (media_type = 3)",
                )
            )
        }

        return contentResolver.queryFlow(
            DataConstants.CONTENT_URI,
            DataConstants.ALBUM_PROJECTION,
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
                        val thumbnailPathIndex =
                            it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val thumbnailRelativePathIndex =
                            it.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                        val thumbnailDateIndex =
                            it.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                        val sizeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                        val mimeTypeIndex =
                            it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

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

    private fun Flow<Cursor?>.mapToMedia(): Flow<List<Media>> {
        return mapEachRow(DataConstants.MEDIA_PROJECTION) { it, indexCache ->
            var i = 0

            val id = it.getLong(indexCache[i++])
            val path = it.getString(indexCache[i++])
            val relativePath = it.getString(indexCache[i++])
            val title = it.getString(indexCache[i++])
            val albumID = it.getLong(indexCache[i++])
            val albumLabel = it.tryGetString(indexCache[i++], Build.MODEL)
            val mimeType = it.getString(indexCache[i++])
            val contentUri = if (mimeType.contains("image"))
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val uri = ContentUris.withAppendedId(contentUri, id)
            Media(
                id = id,
                label = title,
                uri = uri,
                path = path,
                relativePath = relativePath,
                albumID = albumID,
                albumLabel = albumLabel ?: Build.MODEL,
                mimeType = mimeType
            )
        }
    }

    override fun getMedia(albumId: Long): Flow<Result<List<Media>>> {
        val args = listOf(albumId.toString()).toTypedArray()
        val queryArgs = Bundle().apply {
            putAll(
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to "((media_type = 1) OR (media_type = 3)) AND (bucket_id = ?)",
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to args,
                )
            )
        }

        return contentResolver.queryFlow(
            DataConstants.CONTENT_URI,
            DataConstants.MEDIA_PROJECTION,
            queryArgs
        )
            .mapToMedia()
            .map {
                Result.success(it)
            }
            .catch {
                Result.failure<List<Media>>(it)
            }
    }
}

fun Cursor?.tryGetString(columnIndex: Int, fallback: String? = null): String? {
    return this?.getStringOrNull(columnIndex) ?: fallback
}