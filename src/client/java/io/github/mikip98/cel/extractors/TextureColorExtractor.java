package io.github.mikip98.cel.extractors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.structures.ColorReturn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class TextureColorExtractor {

    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(4096)
            .expireAfterAccess(Constants.colorCacheTimeoutMinutes, TimeUnit.MINUTES) // Time-based expiration to reduce library memory usage during non-use
            .build();

    public static ColorReturn getAverageTextureColor(String modId, String textureId, AVGTypes avgType) {
        String cacheKey = modId + "_" + textureId;

        // Check the cache first
        ColorReturn colorReturn = colorCache.getIfPresent(cacheKey);

        if (colorReturn == null) {
            // Get blockstate path
            AssetPathResolver.AssetPaths texturePaths = AssetPathResolver.getTexturePaths(modId, textureId);

            if (texturePaths == null || texturePaths.jarPaths == null || texturePaths.jarPaths.isEmpty() || texturePaths.assetPath == null || texturePaths.assetPath.isEmpty()) {
                LOGGER.warn("Failed to get model paths for model `{}` from mod `{}`", textureId, modId);
                return null;
            }
            colorReturn = new ColorReturn();

            LOGGER.info("Texture path: {} in mod files: {}", texturePaths.assetPath, texturePaths.jarPaths);

            for (String jarPath : texturePaths.jarPaths) {

            }

            // Store the result in the cache
            colorCache.put(cacheKey, colorReturn);
        }

        return colorReturn;
    }
}
