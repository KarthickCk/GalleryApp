package com.app.gallery.domain.usecase

import com.app.gallery.domain.model.Media
import com.app.gallery.domain.repository.IGalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetMediaUseCase @Inject constructor(
    private val iGalleryRepository: IGalleryRepository
) {

    fun invoke(albumId: Long): Flow<Result<List<Media>>> {
        return iGalleryRepository.getMedia(albumId)
            .flowOn(Dispatchers.IO)
    }
}