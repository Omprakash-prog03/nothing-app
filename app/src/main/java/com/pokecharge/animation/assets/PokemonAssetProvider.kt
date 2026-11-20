package com.pokecharge.animation.assets

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodes Pokémon artwork directly from `assets/` with no network access
 * and no disk cache needed (assets are already local and WebP-optimised by
 * the asset pipeline - see assets/ASSET_SOURCES.md).
 *
 * A small in-memory [LruCache] bounds total decoded-bitmap memory instead
 * of caching every one of the 36 artworks at full resolution forever,
 * which matters on a phone-class heap. Thumbnails (collection grid) are
 * decoded at a lower sample size than the full-resolution artwork used on
 * the detail/charging screens.
 */
object PokemonAssetProvider {

    private const val THUMBNAIL_TARGET_PX = 180
    private const val FULL_TARGET_PX = 640

    // Cache size in kilobytes of decoded pixel data (~6MB budget).
    private val cache = object : LruCache<String, ImageBitmap>(6 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            (value.width * value.height * 4) / 1024
    }

    suspend fun loadThumbnail(context: Context, assetPath: String): ImageBitmap? =
        load(context, assetPath, THUMBNAIL_TARGET_PX, "thumb:$assetPath")

    suspend fun loadFull(context: Context, assetPath: String): ImageBitmap? =
        load(context, assetPath, FULL_TARGET_PX, "full:$assetPath")

    private suspend fun load(context: Context, assetPath: String, targetPx: Int, cacheKey: String): ImageBitmap? {
        cache.get(cacheKey)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val appContext = context.applicationContext
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                appContext.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, boundsOptions) }

                val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, targetPx)
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = appContext.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, decodeOptions) }
                    ?: return@withContext null

                val imageBitmap = bitmap.asImageBitmap()
                cache.put(cacheKey, imageBitmap)
                imageBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, targetPx: Int): Int {
        var sampleSize = 1
        var currentMax = maxOf(width, height)
        while (currentMax / (sampleSize * 2) >= targetPx) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
