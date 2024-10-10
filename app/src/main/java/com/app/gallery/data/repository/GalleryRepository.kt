package com.app.gallery.data.repository

import android.content.ContentResolver
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GalleryRepository @Inject constructor(
    private val contentResolver: ContentResolver,
) : IGalleryRepository {

    override fun getAlbums(): Flow<Result<List<Album>>> {
        return flow {
            emit(Result.success(emptyList()))
        }
    }
}