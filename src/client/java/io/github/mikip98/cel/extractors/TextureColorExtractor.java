package io.github.mikip98.cel.extractors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.structures.ColorRGBA;
import io.github.mikip98.cel.structures.ColorReturn;
import io.github.mikip98.cel.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class TextureColorExtractor extends BaseColorExtractor {

    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(4096)
            .expireAfterAccess(Util.colorCacheTimeoutMinutes, TimeUnit.MINUTES) // Time-based expiration to reduce library memory usage during non-use
            .build();

    public static void clearCache() { colorCache.invalidateAll(); }


    public static ColorReturn getAverageTextureColor(String modId, String textureId, float weightedness, AVGTypes avgType) {
        String cacheKey = modId + "_" + textureId;

        // Check the cache first
        ColorReturn colorReturn = colorCache.getIfPresent(cacheKey);

        if (colorReturn == null) {
            // Get blockstate path
            AssetPathResolver.AssetPaths texturePaths = AssetPathResolver.getTexturePaths(modId, textureId);

            if (texturePaths == null || texturePaths.jarPaths == null || texturePaths.jarPaths.isEmpty() || texturePaths.assetPath == null || texturePaths.assetPath.isEmpty()) {
                LOGGER.error("Failed to get texture paths for model `{}` from mod `{}`", textureId, modId);
                return null;
            }
            colorReturn = new ColorReturn();

//            LOGGER.info("Texture path: {} in mod files: {}", texturePaths.assetPath, texturePaths.jarPaths);

            int totalProcessedTextureCount = 0;
            for (String jarPath : texturePaths.jarPaths) {
                ColorReturn textureColorReturn = getAverageTextureColorFromJar(jarPath, texturePaths.assetPath, avgType);
                if (textureColorReturn != null) {
                    ++totalProcessedTextureCount;
                    colorReturn.add(textureColorReturn);
                }
            }
            if (totalProcessedTextureCount == 0) {
                LOGGER.error("Failed to process texture files for texture `{}` from mod `{}`", textureId, modId);
                return null;
            }

            colorReturn.color_avg.divide(totalProcessedTextureCount);

            if (Math.round(colorReturn.color_avg.r) > 255 || Math.round(colorReturn.color_avg.g) > 255 || Math.round(colorReturn.color_avg.b) > 255 || Math.round(colorReturn.color_avg.a) > 255) {
                LOGGER.error("Color values are out of range: {}; {}; {}; {}", colorReturn.color_avg.r, colorReturn.color_avg.g, colorReturn.color_avg.b, colorReturn.color_avg.a);
                throw new RuntimeException("Color values are out of range");
            }

            colorReturn.color_avg = postProcessData(colorReturn, weightedness);

            if (Math.round(colorReturn.color_avg.r) > 255 || Math.round(colorReturn.color_avg.g) > 255 || Math.round(colorReturn.color_avg.b) > 255 || Math.round(colorReturn.color_avg.a) > 255) {
                LOGGER.error("Color values are out of range: {}; {}; {}; {}", colorReturn.color_avg.r, colorReturn.color_avg.g, colorReturn.color_avg.b, colorReturn.color_avg.a);
                throw new RuntimeException("Color values are out of range");
            }

            // Store the result in the cache
            colorCache.put(cacheKey, colorReturn);
        }

        return colorReturn;
    }

    public static ColorReturn getAverageTextureColorFromJar(String jarPath, String texturePath, AVGTypes avgType) {
        // Open the JAR or ZIP file
        try (ZipFile zipFile = new ZipFile(jarPath)) {
            // Get the entry (the JSON file) inside the archive
            ZipEntry entry = zipFile.getEntry(texturePath);

            if (entry != null) {
                // Open an InputStream to read the JSON file
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image != null) {
//                        LOGGER.info("Image successfully read from JAR/ZIP.");
                        return getAverageImageColor(image, avgType);
                    } else {
                        LOGGER.error("Failed to read the image.");
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read the image: {};\nexception: {};\nstacktrace: {}", texturePath, e.getMessage(), e.getStackTrace());
                }
            } else {
                LOGGER.error("JSON file not found in the JAR/ZIP archive.");
            }

        } catch (IOException e) {
            LOGGER.error("Failed to open JAR/ZIP file: {};\nexception: {};\nstacktrace: {}", jarPath, e.getMessage(), e.getStackTrace());
        }

        return null;
    }

    public static ColorReturn getAverageImageColor(BufferedImage image, AVGTypes avgType) {
        ColorRGBA avg_color;
        ColorRGBA color_sum = new ColorRGBA();
        double weight = 0.0;

        switch (avgType) {
            case ARITHMETIC:
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        ColorRGBA pixelColor = pixel2Color(image.getRGB(x, y));
                        if (pixelColor.a == 0) continue;
                        color_sum.add(pixelColor);
                        weight += 1;
                    }
                }
                avg_color = color_sum.copy();
                avg_color.divide(weight);
                break;

            case GEOMETRIC:
                avg_color = new ColorRGBA(1, 1, 1, 1);
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        ColorRGBA pixelColor = pixel2Color(image.getRGB(x, y));
                        if (pixelColor.a == 0) continue;
                        avg_color.multiply(pixelColor);
                        color_sum.add(pixelColor);
                        weight += 1;
                    }
                }
                avg_color.root(weight);
                break;

            case WEIGHTED_ARITHMETIC:
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        ColorRGBA pixelColor = pixel2Color(image.getRGB(x, y));
                        if (pixelColor.a == 0) continue;

                        ColorRGBA normalizedPixelColor = pixelColor.copy();
                        normalizedPixelColor.divide(255);

                        float[] HSB = Color.RGBtoHSB(((int) pixelColor.r), ((int) pixelColor.g), ((int) pixelColor.b), null);
                        float saturation = HSB[1];
                        float brightness = HSB[2];

                        double max_color_value = pixelColor.getMaxRGB();

                        double weight_factor = (
                                Math.sqrt(
                                        Math.pow(normalizedPixelColor.r, 2) +
                                        Math.pow(normalizedPixelColor.g, 2) +
                                        Math.pow(normalizedPixelColor.b, 2)
                                ) *
                                Math.pow(max_color_value, 2) *
                                (0.25 + (saturation * 0.75)) *
                                brightness *
                                normalizedPixelColor.a
                        );

                        pixelColor.multiply(weight_factor);
                        color_sum.add(pixelColor);
                        weight += weight_factor;
                    }
                }
                avg_color = color_sum.copy();
                avg_color.divide(weight);
                break;

            default:
                LOGGER.error("Invalid AVG type: {}", avgType);
                return null;
        }

        return new ColorReturn(avg_color, color_sum, weight);
    }

    public static ColorRGBA pixel2Color(int pixel) {
        return new ColorRGBA(
                (pixel >> 16) & 0xFF,
                (pixel >> 8) & 0xFF,
                pixel & 0xFF,
                (pixel >> 24) & 0xFF
        );
    }
}
