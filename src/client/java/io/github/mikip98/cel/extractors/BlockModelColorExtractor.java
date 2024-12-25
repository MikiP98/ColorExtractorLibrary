package io.github.mikip98.cel.extractors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.structures.ColorReturn;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BlockModelColorExtractor {

    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(Constants.colorCacheTimeoutMinutes, TimeUnit.MINUTES) // Time-based expiration to reduce library memory usage during non-use
            .build();

    public static @NotNull ColorReturn getAverageModelColor(String modId, String modelId, AVGTypes avgType) {
        return null;
    }
}
