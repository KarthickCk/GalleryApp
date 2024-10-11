package com.app.gallery.presentation.gallery

import com.app.gallery.domain.model.Album
import com.app.gallery.domain.model.Media
import com.app.gallery.domain.usecase.GetAlbumsUseCase
import com.app.gallery.domain.usecase.GetMediaUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GalleryViewModelTest {

    @MockK
    lateinit var getMediaUseCase: GetMediaUseCase

    @MockK
    lateinit var getAlbumsUseCase: GetAlbumsUseCase

    private lateinit var galleryViewModel: GalleryViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        val dispatcher = TestCoroutineDispatcher()
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)
        galleryViewModel = spyk(GalleryViewModel(getAlbumsUseCase, getMediaUseCase))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadAlbums success`(): Unit = runTest {

        coEvery { getAlbumsUseCase.invoke() } returns flowOf(
            Result.success(
                listOf(
                    Album(
                        label = "",
                        pathToThumbnail = "",
                        uri = mockk(),
                        relativePath = ""
                    )
                )
            )
        )

        galleryViewModel.loadAlbums()

        Assert.assertTrue(galleryViewModel.uiState.value is UiState.Data)
    }

    @Test
    fun `test loadAlbums error`(): Unit = runTest {

        coEvery { getAlbumsUseCase.invoke() } returns flowOf(
            Result.failure(Exception())
        )

        galleryViewModel.loadAlbums()

        Assert.assertTrue(galleryViewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `test loadAlbums catch`(): Unit = runTest {

        coEvery { getAlbumsUseCase.invoke() } returns flow {
            throw Exception()
        }

        galleryViewModel.loadAlbums()

        Assert.assertTrue(galleryViewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `test loadMedia success`(): Unit = runTest {

        coEvery { getMediaUseCase.invoke(123) } returns flowOf(
            Result.success(
                listOf(
                    Media(
                        label = "",
                        uri = mockk(),
                        relativePath = "",
                        albumID = 12,
                        albumLabel = "",
                        mimeType = "",
                        path = ""
                    )
                )
            )
        )

        galleryViewModel.loadMedia(123)

        Assert.assertTrue(galleryViewModel.mediaState.value is UiState.Data)
    }

    @Test
    fun `test loadMedia error`(): Unit = runTest {

        coEvery { getMediaUseCase.invoke(123) } returns flowOf(
            Result.failure(Exception())
        )

        galleryViewModel.loadMedia(123)

        Assert.assertTrue(galleryViewModel.mediaState.value is UiState.Error)
    }

    @Test
    fun `test loadMedia catch`(): Unit = runTest {

        coEvery { getMediaUseCase.invoke(123) } returns flow {
            throw Exception()
        }

        galleryViewModel.loadMedia(123)

        Assert.assertTrue(galleryViewModel.mediaState.value is UiState.Error)
    }
}