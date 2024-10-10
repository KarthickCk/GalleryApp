package com.app.gallery.di

import android.content.ContentResolver
import android.content.Context
import com.app.gallery.data.repository.GalleryRepository
import com.app.gallery.domain.repository.IGalleryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    fun providesGalleryRepository(galleryRepository: GalleryRepository): IGalleryRepository {
        return galleryRepository
    }
}