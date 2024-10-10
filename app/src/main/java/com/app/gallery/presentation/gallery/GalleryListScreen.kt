package com.app.gallery.presentation.gallery

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.gallery.presentation.components.AlbumComponent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import com.app.gallery.R

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GalleryList(
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {

    val context = LocalContext.current

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.app_name))
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
}