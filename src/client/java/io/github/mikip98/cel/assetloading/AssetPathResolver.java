package io.github.mikip98.cel.assetloading;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class AssetPathResolver {
    // Mod ID -> Asset Type -> Assets -> List of jars that include this asset
    public static Map<String, Map<String, Map<String, List<String>>>> assetPaths = new HashMap<>();


    public static void cachePathsIfNotCached() {
        if (assetPaths.isEmpty()) {
            updatePathCache();
        }
    }

    public static boolean updatePathCache() {
        HashSet<String> cachedAssetTypes = new HashSet<>(Arrays.asList("blockstates", "models", "textures"));

        Map<String, Map<String, Map<String, List<String>>>> newAssetPaths = new HashMap<>();

        // Get the mods directory path
        Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");

        // Go through every `.jar` and `.zip` file in the `mods` directory
        try (Stream<Path> paths = Files.list(modsFolder)) {
            paths.filter(file -> file.toString().endsWith(".jar") || file.toString().endsWith(".zip")).forEach(modPath -> {
                if (modPath.toString().endsWith(".jar") || modPath.toString().endsWith(".zip")) {
                    handleZipOrJar(modPath.toFile(), cachedAssetTypes, newAssetPaths);
                }
            });
        } catch (IOException e) {
            // LOG the error
            LOGGER.error("Failed to update asset path cache!");
            return false;
        }

        assetPaths = newAssetPaths;

        final int size = getCacheSize();
        LOGGER.info("Path cache updated! Bytes: {}; Kilobytes: {}; Megabytes: {}", size,
                ((float) Math.round(((float) size) / 1024 * 10)) / 10,
                ((float) Math.round(((float) size) / 1024 / 1024 * 10)) / 10
        );

        return true;
    }

    private static int getCacheSize() {
        int size = 0;
//        LOGGER.info("Asset path cache updated!");
//        LOGGER.info("Found `{}` mods:", assetPaths.size());
        for (Map.Entry<String, Map<String, Map<String, List<String>>>> entry : assetPaths.entrySet()) {
            size += entry.getKey().getBytes().length;
//            LOGGER.info("  - ModID: {}", entry.getKey());
            for (Map.Entry<String, Map<String, List<String>>> assetEntry : entry.getValue().entrySet()) {
                size += assetEntry.getKey().getBytes().length;
//                LOGGER.info("    - Asset entry: {}; Length: {}", assetEntry.getKey(), assetEntry.getValue().size());
                for (Map.Entry<String, List<String>> asset : assetEntry.getValue().entrySet()) {
                    size += asset.getKey().getBytes().length;
//                    LOGGER.info("      - Asset: {}", asset);
                }
            }
        }
        return size;
    }

    public static void handleZipOrJar(File file, HashSet<String> cachedAssetTypes, Map<String, Map<String, Map<String, List<String>>>> newAssetPaths) {
        try (ZipFile zipFile = new ZipFile(file)) {

            String lastModID = null;
            Map<String, Map<String, List<String>>> entryTypes = new HashMap<>();

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().startsWith("assets/") && !entry.isDirectory()) {
                    String[] parts = entry.getName().split("/", 4);
                    String currentModID = parts[1];

                    if (!Objects.equals(lastModID, currentModID)) {
                        if (lastModID != null) {
                            addToCache(lastModID, entryTypes, newAssetPaths);
                        }
                        lastModID = currentModID;
                        entryTypes = new HashMap<>();
                    }

                    if (parts.length < 3) {
                        LOGGER.warn("Invalid or not supported entry: {}", entry.getName());
                        continue;
                    }
                    // TODO: fix, default vanilla textures are not loading correctly, only gui/... textures are being cached

                    String entryType = parts[2];

                    if (cachedAssetTypes.contains(entryType)) {
                        String assetId = parts[3];

                        if (entryType.equals("textures")) {
                            String[] assetIdParts = assetId.split("/", 2);
                            if (!(assetIdParts[0].equals("block") || assetIdParts[0].equals("item"))) {
                                continue;
                            }
                        }

//                        LOGGER.info("Caching asset: {}; Under: {}, From mod: {}", assetId, entryType, lastModID);

                        entryTypes.computeIfAbsent(entryType, k -> new HashMap<>()).putIfAbsent(assetId, new ArrayList<>());
                        entryTypes.get(entryType).get(assetId).add(file.getName());
                    }
                }
            }
            // Save any unsaved entries from the last mod
            addToCache(lastModID, entryTypes, newAssetPaths);
        } catch (ZipException e) {
            LOGGER.error("Failed to handle zip or jar (ZIP)!;\n{}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Failed to handle zip or jar (IO)!;\n{}", e.getMessage());
        }
    }

    public static void addToCache(String modId, Map<String, Map<String, List<String>>> entryTypes, Map<String, Map<String, Map<String, List<String>>>> newAssetPaths) {
        if (!entryTypes.isEmpty()) {
            if (!newAssetPaths.containsKey(modId) || newAssetPaths.get(modId) == null) {
                newAssetPaths.put(modId, entryTypes);

            } else {
                Map<String, Map<String, List<String>>> existingEntryTypes = newAssetPaths.get(modId);
                for (Map.Entry<String, Map<String, List<String>>> entryType : existingEntryTypes.entrySet()) {

                    if (!entryTypes.containsKey(entryType.getKey())) {
                        entryTypes.put(entryType.getKey(), entryType.getValue());

                    } else {
                        for (Map.Entry<String, List<String>> entryMap : entryType.getValue().entrySet()) {

                            if (!entryTypes.get(entryMap.getKey()).containsKey(entryMap.getKey())) {
                                entryTypes.get(entryMap.getKey()).put(entryMap.getKey(), entryMap.getValue());

                            } else {
                                entryTypes.get(entryMap.getKey()).get(entryMap.getKey()).addAll(entryMap.getValue());
                            }
                        }
                    }
                }
                newAssetPaths.put(modId, entryTypes);
            }
        }
//        LOGGER.info("Finished caching mod: {}; Found {} entry types", lastModID, entryTypes.keySet());
    }


    public static AssetPaths getBlockstatePaths(String modID, String blockstateID) {
        return processPathRequest(modID, blockstateID, "json", "blockstate");
    }
    public static AssetPaths getModelPaths(String modId, String modelId) {
        return processPathRequest(modId, modelId, "json", "model");
    }
    public static AssetPaths getTexturePaths(String modId, String textureId) {
        return processPathRequest(modId, textureId, "png", "texture");
    }

    public static AssetPaths processPathRequest(String modId, String assetId, String assetExtension, String assetType) {
        assetId += "." + assetExtension;

        if (!assetPaths.containsKey(modId)) {
            LOGGER.warn("Mod `{}` does not exist!", modId);
            return null;
        }

        Map<String, Map<String, List<String>>> modAssetCollection = assetPaths.get(modId);
        if (!modAssetCollection.containsKey("textures")) {
            LOGGER.warn("Mod `{}` does not have any {}s!", modId, assetType);
            return null;
        }

        Map<String, List<String>> assets = modAssetCollection.get(assetType + "s");
        if (!assets.containsKey(assetId)) {
            LOGGER.warn("{} `{}` does not exist!", new StringBuilder().append(Character.toTitleCase(assetType.charAt(0))).append(assetType.substring(1)), assetId);
//            LOGGER.warn("Available {}s: {}", assetType, assets);
            return null;
        }

        List<String> files = assets.get(assetId);
//        LOGGER.info("Found {} `{}` in {} files: {}", assetType, assetId, files.size(), files);

        return generatePaths(files, modId, assetType + "s", assetId);
    }

    public static AssetPaths generatePaths(List<String> modFiles, String modID, String assetType, String assetID) {
        AssetPaths paths = new AssetPaths();

        paths.jarPaths = new ArrayList<>();
        paths.assetPath = "assets/" + modID + "/" + assetType + "/" + assetID;

        Path modDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        for (String file : modFiles) {
            paths.jarPaths.add(modDir.resolve(file).toString());
        }

        return paths;
    }

    public static class AssetPaths {
        public String assetPath;
        public List<String> jarPaths;
    }
}
