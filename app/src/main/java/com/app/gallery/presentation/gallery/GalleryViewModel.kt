package com.app.gallery.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gallery.domain.model.Album
import com.app.gallery.domain.model.Media
import com.app.gallery.domain.usecase.GetAlbumsUseCase
import com.app.gallery.domain.usecase.GetMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getMediaUseCase: GetMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryState>(GalleryState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Loading)
    val mediaState = _mediaState.asStateFlow()

    fun loadAlbums() {
        viewModelScope.launch {
            getAlbumsUseCase.invoke()
                .onStart {
                    _uiState.value = GalleryState.Loading
                }
                .onEach {
                    if (it.isSuccess) {
                        _uiState.value = GalleryState.Albums(it.getOrNull() ?: emptyList())
                    } else
                        _uiState.value =
                            GalleryState.Error(it.exceptionOrNull()?.message ?: "Error")
                }
                .catch {
                    _uiState.value =
                        GalleryState.Error(it.message ?: "Error")
                }
                .collect()
        }
    }

    fun loadMedia(albumId: Long) {
        viewModelScope.launch {
            getMediaUseCase.invoke(albumId)
                .onEach {
                    if (it.isSuccess) {
                        _mediaState.value = MediaState.Medias(it.getOrNull() ?: emptyList())
                    } else
                        _mediaState.value =
                            MediaState.Error(it.exceptionOrNull()?.message ?: "Error")
                }
                .collect()
        }
    }
}

sealed class GalleryState {
    data class Albums(val list: List<Album>) : GalleryState()
    data class Error(val message: String) : GalleryState()
    data object Loading : GalleryState()
}

sealed class MediaState {
    data class Medias(val list: List<Media>) : MediaState()
    data class Error(val message: String) : MediaState()
    data object Loading : MediaState()
}