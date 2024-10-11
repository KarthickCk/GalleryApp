package com.app.gallery.presentation.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AlbumDetailScreen(
    albumId: Long,
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {

    LaunchedEffect(key1 = albumId) {
        galleryViewModel.loadMedia(albumId)
    }
}