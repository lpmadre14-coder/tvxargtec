package com.tvxargtec.online.utils

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule
import java.io.File

@GlideModule
class GlideCacheConfig : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val cacheDir = File(context.cacheDir, "glide_logo_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        builder.setDiskCache(DiskLruCacheFactory(cacheDir.absolutePath, 50 * 1024 * 1024))
    }
}
