package com.app.gallery.domain.usecase

import com.app.gallery.domain.model.Album
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val iGalleryRepository: IGalleryRepository
) {
    fun invoke(): Flow<Result<List<Album>>> {
        return iGalleryRepository.getAlbums()
            .flowOn(Dispatchers.IO)
    }
}