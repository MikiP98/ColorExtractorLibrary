package io.github.mikip98.cel.extractors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.structures.ColorRGBA;
import io.github.mikip98.cel.structures.ColorReturn;

import java.util.concurrent.TimeUnit;

public class BlockstateColorExtractor {

    // Create a Guava cache with a size limit of 512 and automatic eviction of least-recently used items
    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(512) // Limit the cache to 512 entries
            .expireAfterAccess(10, TimeUnit.MINUTES) // Optionally, set time-based expiration (if needed)
            .build();


    public static ColorRGBA getAverageBlockstateColor(String modId, String blockstateId, float weightedness, AVGTypes avgType) {
        String cacheKey = modId + "_" + blockstateId;

        // Check the cache first
        ColorReturn color = colorCache.getIfPresent(cacheKey);
        if (color != null) {
            return postProcessData(color, weightedness);
        }

        ColorReturn colorReturn = BlockModelColorExtractor.getAverageModelColor(modId, blockstateId, avgType);

        // Store the result in the cache
        colorCache.put(cacheKey, colorReturn);

        return postProcessData(colorReturn, weightedness);
    }

    public static ColorRGBA postProcessData(ColorReturn colorReturn, float weightedness) {
        ColorRGBA color_weighted_avg = colorReturn.color_avg;

        ColorRGBA color_sum = colorReturn.color_sum;
        double weight_sum = colorReturn.weight_sum;
        ColorRGBA color_avg = new ColorRGBA(
                color_sum.r / weight_sum,
                color_sum.g / weight_sum,
                color_sum.b / weight_sum,
                color_sum.a / weight_sum
        );

        return new ColorRGBA(
                color_weighted_avg.r * weightedness + color_avg.r * (1 - weightedness),
                color_weighted_avg.g * weightedness + color_avg.g * (1 - weightedness),
                color_weighted_avg.b * weightedness + color_avg.b * (1 - weightedness),
                color_weighted_avg.a * weightedness + color_avg.a * (1 - weightedness)
        );
    }

    public static void clearCache() {
        colorCache.invalidateAll();
    }
}
