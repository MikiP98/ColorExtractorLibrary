package io.github.mikip98.del.api;

import io.github.mikip98.del.assetloading.AssetPathResolver;
import io.github.mikip98.del.extractors.color.BlockModelColorExtractor;
import io.github.mikip98.del.extractors.color.BlockstateColorExtractor;
import io.github.mikip98.del.extractors.color.TextureColorExtractor;
import io.github.mikip98.del.util.Cache;
import io.netty.util.internal.UnstableApi;

import static io.github.mikip98.del.DataExtractionLibraryClient.LOGGER;

// This is the one of the classed that mods using this lib should use/one of the ones that guarantee stability
@SuppressWarnings("unused")
public class CacheAPI {

    // ------------------------------------------------------------------------
    // --------------------------------- SETUP --------------------------------
    // ------------------------------------------------------------------------

    /**
     * Initializes the path cache if it hasn't been initialized yet
     */
    public static void cachePathsIfNotCached() {
        AssetPathResolver.cachePathsIfNotCached();
    }

    /**
     * Updates the path cache
     */
    public static boolean updatePathCache() { return AssetPathResolver.updatePathCache(); }

    /**
     * DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING!!!
     * This method clears all the caches used by this library.
     * This can free up a lot of memory, but severely limits the application of this library.
     * This library was made as a one time use tool, and should not be used in a permanent way.
     * Use this only if your mod is always initializing the cache,
     * and you don't want to steal the memory from the user after you're finished.
     */
    @UnstableApi
    public static void clearPathCache() {
        BlockstateColorExtractor.clearCache();
        LOGGER.warn("Blockstate color cache cleared through the API!");

        BlockModelColorExtractor.clearCache();
        LOGGER.warn("Block model color cache cleared through the API!");

        TextureColorExtractor.clearCache();
        LOGGER.warn("Texture color cache cleared through the API!");

        AssetPathResolver.assetPaths.clear();
        LOGGER.warn("Path cache cleared through the API!");
    }
    /**
     * DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING!!!
     * This method clears all the caches used by this library.
     * This can free up a lot of memory, but severely limits the application of this library.
     * This library was made as a one time use tool, and should not be used in a permanent way.
     * Use this only if your mod is always initializing the cache,
     * and you don't want to steal the memory from the user after you're finished.
     *
     * @param cache The cache to clear
     */
    @UnstableApi
    public static void clearPathCache(Cache cache) {
        switch (cache) {
            case PATH_CACHE -> {
                AssetPathResolver.assetPaths.clear();
                LOGGER.warn("Path cache cleared through the API!");
            }
            case BLOCKSTATE_EXTRACTOR_CACHE -> {
                BlockstateColorExtractor.clearCache();
                LOGGER.warn("Blockstate color cache cleared through the API!");
            }
            case BLOCKMODEL_EXTRACTOR_CACHE -> {
                BlockModelColorExtractor.clearCache();
                LOGGER.warn("Block model color cache cleared through the API!");
            }
            case TEXTURE_EXTRACTOR_CACHE -> {
                TextureColorExtractor.clearCache();
                LOGGER.warn("Texture color cache cleared through the API!");
            }
            default -> LOGGER.warn("Cache `{}` does not exist!", cache.name());
        }
    }



    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
