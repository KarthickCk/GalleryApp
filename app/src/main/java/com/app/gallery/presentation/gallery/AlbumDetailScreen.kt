package com.app.gallery.presentation.gallery

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gallery.domain.model.Media
import com.app.gallery.presentation.components.MediaComponent

@Composable
fun AlbumDetailScreen(
    albumName: String,
    albumId: Long,
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {

    val mediaState = galleryViewModel.mediaState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = albumId) {
        galleryViewModel.loadMedia(albumId)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when (mediaState.value) {

            is UiState.Data -> {
                MediaList(
                    name = albumName,
                    list = (mediaState.value as UiState.Data).value
                )
            }

            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaList(
    name: String,
    list: List<Media>
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TopAppBar(
            title = {
                Text(name)
            },
            navigationIcon = {

                IconButton(onClick = {
                    (context as? Activity)?.onBackPressed()
                }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Localized description"
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4)
        ) {
            items(list) {
                MediaComponent(media = it)
            }
        }
    }
}