package io.github.mikip98.del.extractors.color;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.mikip98.del.assetloading.AssetPathResolver;
import io.github.mikip98.del.enums.AVGTypes;
import io.github.mikip98.del.structures.ColorReturn;
import io.github.mikip98.del.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.github.mikip98.del.DataExtractionLibraryClient.LOGGER;

public class BlockModelColorExtractor extends BaseColorExtractor {

    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(Util.colorCacheTimeoutMinutes, TimeUnit.MINUTES) // Time-based expiration to reduce library memory usage during non-use
            .build();

    public static void clearCache() { colorCache.invalidateAll(); }


    public static ColorReturn getAverageModelColor(String modId, String modelId, float weightedness, AVGTypes avgType) {
        String cacheKey = modId + "_" + modelId;

        // Check the cache first
        ColorReturn colorReturn = colorCache.getIfPresent(cacheKey);

        if (colorReturn == null) {
            // Get blockstate path
            AssetPathResolver.AssetPaths modelPaths = AssetPathResolver.getModelPaths(modId, modelId);

            if (modelPaths == null || modelPaths.jarPaths == null || modelPaths.jarPaths.isEmpty() || modelPaths.assetPath == null || modelPaths.assetPath.isEmpty()) {
                LOGGER.error("Failed to get model paths for model `{}` from mod `{}`", modelId, modId);
                return null;
            }
            colorReturn = new ColorReturn();

//            LOGGER.info("Model path: {} in mod files: {}", modelPaths.assetPath, modelPaths.jarPaths);

            int totalTexturePathCount = 0;
            String assetPath = modelPaths.assetPath;
            for (String jarPath : modelPaths.jarPaths) {
                Collection<String> texturePaths = extractTexturePathsFromModel(jarPath, assetPath).values();
//                LOGGER.info("Texture paths: {}", texturePaths);

                if (!texturePaths.isEmpty()) {
                    for (String texturePath : texturePaths) {
                        String textureModId;
                        String texturePathId;
                        String[] texturePathSplit = texturePath.split(":", 2);

                        if (texturePathSplit.length == 1) {
                            textureModId = "minecraft";
                            texturePathId = texturePathSplit[0];
                        } else {
                            textureModId = texturePathSplit[0];
                            texturePathId = texturePathSplit[1];
                        }

                        ColorReturn textureColor = TextureColorExtractor.getAverageTextureColor(textureModId, texturePathId, weightedness, avgType);
                        if (textureColor != null) {
                            ++totalTexturePathCount;
                            colorReturn.add(textureColor);
                        }
//                        LOGGER.info("Model path: {}; From mod: {}; Color: {}", texturePathId, textureModId, colorReturn);
                    }
                }
            }
            if (totalTexturePathCount == 0) {
                LOGGER.error("Failed to get texture paths for model `{}` from mod `{}`; Or failed to process all its textures", modelId, modId);
                return null;
            }
//            LOGGER.info("Total texture path count: {}", totalTexturePathCount);

            colorReturn.color_avg.divide(totalTexturePathCount);
            colorReturn.color_avg = postProcessData(colorReturn, weightedness);

            // Store the result in the cache
            colorCache.put(cacheKey, colorReturn);
        }

        return colorReturn;
    }

    public static @NotNull Map<String, String> extractTexturePathsFromModel(String jarPath, String modelPath) {
//        LOGGER.info("Extracting texture paths from model: {}", modelPath);
//        LOGGER.info("In jar: {}", jarPath);
        Map<String, String> texturePaths = new HashMap<>();

        // Create a Gson instance
        Gson gson = new Gson();

        // Open the JAR or ZIP file
        try (ZipFile zipFile = new ZipFile(jarPath)) {
            // Get the entry (the JSON file) inside the archive
            ZipEntry entry = zipFile.getEntry(modelPath);

            if (entry != null) {
                // Open an InputStream to read the JSON file
                try (InputStream inputStream = zipFile.getInputStream(entry); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    JsonObject modelJson = gson.fromJson(reader, JsonObject.class);  // Or use your specific class
//                    LOGGER.info("Block model JSON: {}", modelJson);

                    if (modelJson.has("parent")) {
                        String[] parentModelPathParts = modelJson.get("parent").getAsString().split(":", 2);
                        String modId;
                        String parentModelId;

                        if (parentModelPathParts.length == 1) {
                            modId = "minecraft";
                            parentModelId = parentModelPathParts[0];
                        } else {
                            modId = parentModelPathParts[0];
                            parentModelId = parentModelPathParts[1];
                        }

                        // TODO: Make a better support for overriden parents
                        AssetPathResolver.AssetPaths parentModelPaths = AssetPathResolver.getModelPaths(modId, parentModelId);
                        for (String parentJarPath : parentModelPaths.jarPaths) {
                            Map<String, String> parentTexturePaths = extractTexturePathsFromModel(parentJarPath, parentModelPaths.assetPath);
//                            texturePaths.putAll(parentTexturePaths);
                            for (Map.Entry<String, String> parentTexturePath : parentTexturePaths.entrySet()) {
                                if (parentTexturePath.getValue().startsWith("#")) continue;
                                texturePaths.put(parentTexturePath.getKey(), parentTexturePath.getValue());
                            }
                        }
//                        texturePaths = (extractTexturePathsFromModel(jarPath, parentModelPaths));
                    }

                    if (modelJson.has("textures")) {
                        JsonObject textures = (JsonObject) modelJson.get("textures");
//                        Set<String> textureKeys = textures.keySet();
//                        LOGGER.info("Textures: " + textures.entrySet());

                        // TODO: Add weight from model faces, etc.
                        // TODO: Add textures from parent models support

                        for (Map.Entry<String, JsonElement> textureEntry : textures.entrySet()) {
                            texturePaths.put(textureEntry.getKey(), textureEntry.getValue().getAsString());
                        }
                    }/* else {
                        LOGGER.error("Model JSON '{}' in mod file '{}' does not contain 'textures'!", modelPath, jarPath);
                    }*/
                } catch (IOException e) {
                    LOGGER.error("Failed to read JSON file: {};\nException: {};\nStacktrace: {}", modelPath, e.getMessage(), e.getStackTrace());
                }
            } else {
                LOGGER.error("Model JSON file '{}' not found in the JAR/ZIP archive.", modelPath);
            }

        } catch (IOException e) {
            LOGGER.error("Failed to open JAR/ZIP file: {};\nexception: {};\nstacktrace: {}", jarPath, e.getMessage(), e.getStackTrace());
        }

//        LOGGER.info("Extracted {} texture paths from model file: {}", texturePaths.size(), modelPath);
//        LOGGER.info("Texture paths: {}", texturePaths);

        return texturePaths;
    }
}
