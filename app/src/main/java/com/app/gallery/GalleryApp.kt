package com.app.gallery

import android.app.Application
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.decode.supportVideoFrame
import com.github.panpf.sketch.util.appCacheDirectory
import dagger.hilt.android.HiltAndroidApp
import okio.FileSystem

@HiltAndroidApp
class GalleryApp: Application(), SingletonSketch.Factory {

    override fun createSketch(context: PlatformContext): Sketch = Sketch.Builder(this).apply {
        components {
            supportVideoFrame()
        }
        val diskCache = DiskCache.Builder(context, FileSystem.SYSTEM)
            .directory(context.appCacheDirectory())
            .maxSize(150 * 1024 * 1024).build()

        resultCache(diskCache)
        downloadCache(diskCache)
    }.build()

}