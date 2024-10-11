package com.app.gallery.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.gallery.presentation.gallery.AlbumDetailScreen
import com.app.gallery.presentation.gallery.GalleryList
import com.app.gallery.presentation.theme.GalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    val navController = rememberNavController()

                    CompositionLocalProvider(LocalNavController provides navController) {
                        NavHost(
                            navController = navController,
                            startDestination = GalleryDestinations.GALLERY_LIST
                        ) {
                            composable(route = GalleryDestinations.GALLERY_LIST) {
                                GalleryList()
                            }

                            composable(
                                route = GalleryDestinations.ALBUM_DETAILS,
                                arguments = listOf(
                                    navArgument(
                                        name = "albumId"
                                    ) {
                                        type = NavType.LongType
                                        defaultValue = 0
                                    },
                                )
                            ) {
                                val id = it.arguments?.getLong("albumId") ?: 0
                                AlbumDetailScreen(albumId = id)
                            }
                        }
                    }

                }
            }
        }
    }
}

val LocalNavController = compositionLocalOf<NavController> { error("No NavController found!") }


object GalleryDestinations {
    val GALLERY_LIST = "screenList"
    val ALBUM_DETAILS = "albumDetails?albumId={albumId}"
}