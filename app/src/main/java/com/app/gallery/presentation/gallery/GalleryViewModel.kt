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

    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _mediaState = MutableStateFlow<UiState<List<Media>>>(UiState.Loading)
    val mediaState = _mediaState.asStateFlow()

    fun loadAlbums() {
        viewModelScope.launch {
            getAlbumsUseCase.invoke()
                .onStart {
                    _uiState.value = UiState.Loading
                }
                .onEach {
                    if (it.isSuccess) {
                        _uiState.value = UiState.Data(it.getOrNull() ?: emptyList())
                    } else
                        _uiState.value =
                            UiState.Error(it.exceptionOrNull()?.message ?: "Error")
                }
                .catch {
                    _uiState.value =
                        UiState.Error(it.message ?: "Error")
                }
                .collect()
        }
    }

    fun loadMedia(albumId: Long) {
        viewModelScope.launch {
            getMediaUseCase.invoke(albumId)
                .onEach {
                    if (it.isSuccess) {
                        _mediaState.value = UiState.Data(it.getOrNull() ?: emptyList())
                    } else
                        _mediaState.value =
                            UiState.Error(it.exceptionOrNull()?.message ?: "Error")
                }
                .catch {
                    _mediaState.value =
                        UiState.Error(it.message ?: "Error")
                }
                .collect()
        }
    }
}

sealed class UiState<out T> {
    data class Data<out R>(val value: R): UiState<R>()
    data object Loading : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}