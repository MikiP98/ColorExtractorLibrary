package io.github.mikip98.cel.extractors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.structures.ColorRGBA;
import io.github.mikip98.cel.structures.ColorReturn;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class BlockstateColorExtractor {

    private static final Cache<String, ColorReturn> colorCache = CacheBuilder.newBuilder()
            .maximumSize(32)
            .expireAfterAccess(Constants.colorCacheTimeoutMinutes, TimeUnit.MINUTES) // Time-based expiration to reduce library memory usage during non-use
            .build();


    @SuppressWarnings("rawtypes")
    public static @NotNull ColorReturn getAverageBlockstateColor(String modId, String blockstateId, List<Map<String, Comparable>> requiredPropertySets, float weightedness, AVGTypes avgType) {
        String cacheKey = modId + "_" + blockstateId;

        // Check the cache first
        ColorReturn colorReturn = colorCache.getIfPresent(cacheKey);

        if (colorReturn == null) {
            colorReturn = new ColorReturn();

            // Get blockstate path
            AssetPathResolver.AssetPaths blockstatePaths = AssetPathResolver.getBlockstatePaths(modId, blockstateId);

            if (blockstatePaths == null || blockstatePaths.jarPaths.isEmpty() || blockstatePaths.assetPath == null || blockstatePaths.assetPath.isEmpty()) {
                return new ColorReturn(
                        new ColorRGBA(128, 128, 128, 128),
                        new ColorRGBA(128, 128, 128, 128),
                        1
                );
            }

            LOGGER.info("Blockstate path: {} in mod files: {}", blockstatePaths.assetPath, blockstatePaths.jarPaths);

            String assetPath = blockstatePaths.assetPath;
            for (String jarPath : blockstatePaths.jarPaths) {
                List<String> modelPaths = extractModelPathsFromBlockstate(jarPath, assetPath, requiredPropertySets);
                if (!modelPaths.isEmpty()) {
                    for (String modelPath : modelPaths) {
                        String modelModId;
                        String modelPathId;
                        String[] modelPathSplit = modelPath.split(":", 2);

                        if (modelPathSplit.length == 1) {
                            modelModId = "minecraft";
                            modelPathId = modelPathSplit[0];
                        } else {
                            modelModId = modelPathSplit[0];
                            modelPathId = modelPathSplit[1];
                        }

                        ColorReturn modelColor = BlockModelColorExtractor.getAverageModelColor(modelModId, modelPathId, avgType);
                        LOGGER.info("Model path: {}; From mod: {}; Color: {}", modelPathId, modelModId, colorReturn.toString());
                    }
                }
            }

//            colorReturn.color_avg = postProcessData(colorReturn, weightedness);

            // Store the result in the cache
            colorCache.put(cacheKey, colorReturn);
        }

        return colorReturn;
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

    @SuppressWarnings("rawtypes")
    public static @NotNull List<String> extractModelPathsFromBlockstate(String jarPath, String blockstatePath, List<Map<String, Comparable>> requiredPropertySets) {
        LOGGER.info("Extracting model paths from blockstate: {}", blockstatePath);
        LOGGER.info("In jar: {}", jarPath);
        List<String> modelPaths = new ArrayList<>();

        // Create a Gson instance
        Gson gson = new Gson();

        // Open the JAR or ZIP file
        try (ZipFile zipFile = new ZipFile(jarPath)) {
            // Get the entry (the JSON file) inside the archive
            ZipEntry entry = zipFile.getEntry(blockstatePath);

            if (entry != null) {
                // Open an InputStream to read the JSON file
                try (InputStream inputStream = zipFile.getInputStream(entry); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    JsonObject blockstateJson = gson.fromJson(reader, JsonObject.class);  // Or use your specific class
                    LOGGER.info("Blockstate JSON: {}", blockstateJson);

                    Set<String> keys = blockstateJson.keySet();
                    LOGGER.info("Blockstate JSON keys: {}", keys);

                    if (keys.contains("variants")) {
                        LOGGER.info("Blockstate type: 'variants'");
                        LOGGER.info("Type of variants: {}", blockstateJson.get("variants").getClass().getName());

                        JsonObject variants = blockstateJson.getAsJsonObject("variants");

                        Set<String> variantKeys = variants.keySet();
                        LOGGER.info("Keys: {}", variantKeys);

                        for (String key : variantKeys) {
                            List<String> keyParts = List.of(key.split(","));
                            Map<String, String> keyPartsKeyValuePairs = new HashMap<>();
                            for (String keyPart : keyParts) {
                                String[] keyPartKeyValue = keyPart.split("=");
                                keyPartsKeyValuePairs.put(keyPartKeyValue[0], keyPartKeyValue[1]);
                            }

                            // Check if keyParts contains at least 1 full set of required properties
                            boolean containsRequiredProperties = false;
                            for (Map<String, Comparable> requiredPropertySet : requiredPropertySets) {
                                boolean containsAllRequiredProperties = true;
                                for (Map.Entry<String, Comparable> keyPart : requiredPropertySet.entrySet()) {
                                    if (keyPartsKeyValuePairs.containsKey(keyPart.getKey())) {
                                        if (!keyPart.getValue().toString().equals(keyPartsKeyValuePairs.get(keyPart.getKey()))) {
                                            containsAllRequiredProperties = false;
                                            break;
                                        }
                                    }
                                }
                                if (containsAllRequiredProperties) {
                                    containsRequiredProperties = true;
                                    break;
                                }
                            }

                            if (containsRequiredProperties) {
                                Object modelEntry = variants.get(key);

                                if (modelEntry instanceof JsonArray) {
//                                    LOGGER.info("Model entry is JsonArray");

                                    for (Object elementObj : (JsonArray) modelEntry) {
                                        JsonObject element = (JsonObject) elementObj;
                                        modelPaths.add(element.get("model").getAsString());
                                    }
                                } else {
//                                    LOGGER.info("Model module is JsonObject");

                                    modelPaths.add(((JsonObject) modelEntry).get("model").getAsString());
                                }

                            } else {
//                                LOGGER.info("Key '{}' does not contain required properties: {}", key, requiredPropertySets);
                            }
                        }

                    } else if (keys.contains("multipart")) {
                        LOGGER.info("Blockstate type: 'multipart'");
                        JsonArray multipart = (JsonArray) blockstateJson.get("multipart");

                        // TODO: Add required properties check

                        for (Object partObj : multipart) {
                            JsonObject part = (JsonObject) partObj;
                            Object apply = part.get("apply");
                            if (apply instanceof JsonArray) {
                                LOGGER.info("Apply is JSONArray");

                                for (Object elementObj : (JsonArray) apply) {
                                    JsonObject element = (JsonObject) elementObj;
                                    modelPaths.add(element.get("model").getAsString());
                                }
                            } else {
                                LOGGER.info("Apply is JSONObject");

                                modelPaths.add(((JsonObject) apply).get("model").getAsString());
                            }
                        }

                    } else {
                        LOGGER.error("Blockstate JSON for '{}' in mod '{}' does not contain 'variants' nor 'multipart'!", blockstatePath, jarPath);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                LOGGER.error("JSON file not found in the JAR/ZIP archive.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Extracted {} model paths from blockstate file: {}", modelPaths.size(), blockstatePath);
        LOGGER.info("Model paths: {}", modelPaths);

        return modelPaths;
    }
}
