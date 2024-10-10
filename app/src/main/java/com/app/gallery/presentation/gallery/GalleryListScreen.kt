package com.app.gallery.presentation.gallery

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gallery.presentation.components.AlbumComponent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GalleryList(
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {

    val uiState = galleryViewModel.uiState.collectAsStateWithLifecycle()

    val permissions34 = listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
    )
    val pre34Permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions34
    } else {
        pre34Permissions
    }

    var permissionGranted by remember { mutableStateOf(false) }
    val mediaPermissions = rememberMultiplePermissionsState(permissions) {
        permissionGranted = it.all { item -> item.value }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(permissionGranted) {
        if (permissionGranted.not()) {
            scope.launch {
                mediaPermissions.launchMultiplePermissionRequest()
            }
        } else
            galleryViewModel.loadAlbums()
    }

    when (uiState.value) {

        is GalleryState.Loading -> Unit

        is GalleryState.Albums -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2)
            ) {
                (uiState.value as GalleryState.Albums).list.run {
                    items(this) {
                        AlbumComponent(album = it) {

                        }
                    }
                }
            }
        }

        else -> Unit

    }


}