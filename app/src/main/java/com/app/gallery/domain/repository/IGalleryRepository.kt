package com.app.gallery.domain.repository

import com.app.gallery.domain.model.Album
import com.app.gallery.domain.model.Media
import kotlinx.coroutines.flow.Flow

interface IGalleryRepository {

    fun getAlbums(): Flow<Result<List<Album>>>

    fun getMedia(albumId: Long): Flow<Result<List<Media>>>
}