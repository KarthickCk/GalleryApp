package com.app.gallery.domain.repository

import com.app.gallery.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface IGalleryRepository {
    fun getAlbums(): Flow<Result<List<Album>>>
}