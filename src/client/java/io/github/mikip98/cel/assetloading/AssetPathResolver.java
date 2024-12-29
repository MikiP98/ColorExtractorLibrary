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
    public static boolean arePathsCached = false;
    public static HashSet<Short> pathsLocks = new HashSet<>();

    public static boolean isUpdateQueued = false;
    public static boolean isClearQueued = false;

    public static void cachePathsIfNotCached() {
        if (!arePathsCached) {
            updatePathCache();
        }
    }

    public static boolean updatePathCache() {
        return updatePathCache(false);
    }
    public static boolean updatePathCache(boolean queueTheUpdate) {
        HashSet<String> cachedAssetTypes = new HashSet<>(Arrays.asList("blockstates", "models", "textures"));

        if (pathsLocks.isEmpty()) {
            clearPathCache();

            // Get the mods directory path
            Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");

            // Go through every `.jar` and `.zip` file in the `mods` directory
            try (Stream<Path> paths = Files.list(modsFolder)) {
                paths.filter(file -> file.toString().endsWith(".jar") || file.toString().endsWith(".zip")).forEach(modPath -> {
                    if (modPath.toString().endsWith(".jar") || modPath.toString().endsWith(".zip")) {
                        handleZipOrJar(modPath.toFile(), cachedAssetTypes);
                    }
                });
            } catch (IOException e) {
                // LOG the error
                LOGGER.error("Failed to update asset path cache!");
                isUpdateQueued = true;
                // TODO: Lock the paths for 60s
                return false;
            }

            final int size = getCacheSize();
            LOGGER.info("Path cache updated! Bytes: {}; Kilobytes: {}; Megabytes: {}", size,
                    ((float) Math.round(((float) size) / 1024 * 10)) / 10,
                    ((float) Math.round(((float) size) / 1024 / 1024 * 10)) / 10
            );

            arePathsCached = true;
            return true;
        } else {
            // LOG the error
            LOGGER.error("Asset path cache not updated! Path locks are in place!");
            isClearQueued &= !queueTheUpdate;
            isUpdateQueued = queueTheUpdate;
            return false;
        }
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

    public static void handleZipOrJar(File file, HashSet<String> cachedAssetTypes) {
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
                            addToCache(lastModID, entryTypes);
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
            addToCache(lastModID, entryTypes);
        } catch (ZipException e) {
            LOGGER.error("Failed to handle zip or jar (ZIP)!;\n{}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Failed to handle zip or jar (IO)!;\n{}", e.getMessage());
        }
    }

    public static void addToCache(String modId, Map<String, Map<String, List<String>>> entryTypes) {
        if (!entryTypes.isEmpty()) {
            if (!assetPaths.containsKey(modId) || assetPaths.get(modId) == null) {
                assetPaths.put(modId, entryTypes);

            } else {
                Map<String, Map<String, List<String>>> existingEntryTypes = assetPaths.get(modId);
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
                assetPaths.put(modId, entryTypes);
            }
        }
//        LOGGER.info("Finished caching mod: {}; Found {} entry types", lastModID, entryTypes.keySet());
    }

    public static boolean clearPathCache() {
        return clearPathCache(false);
    }
    public static boolean clearPathCache(boolean queueTheClear) {
        if (pathsLocks.isEmpty()) {
            arePathsCached = false;
            assetPaths.clear();
            return true;
        } else {
            LOGGER.warn("Paths are locked! Clear was not performed!");
            isUpdateQueued &= !queueTheClear;
            isClearQueued = queueTheClear;
            return false;
        }
    }

    public static void addPathsLock(short code) {
        pathsLocks.add(code);
    }
    public static void removePathsLock(short code) {
        pathsLocks.remove(code);
    }


    public static AssetPaths getBlockstatePaths(String modID, String blockstateID) {
        blockstateID = blockstateID + ".json";

//        LOGGER.info("Getting blockstate `{}` from mod `{}`", blockstateID, modID);

        if (!assetPaths.containsKey(modID)) {
            LOGGER.warn("Mod `{}` does not exist!", modID);
            return null;
        }

        Map<String, Map<String, List<String>>> modAssets = assetPaths.get(modID);
        if (!modAssets.containsKey("blockstates")) {
            LOGGER.warn("Mod `{}` does not have any blockstates!", modID);
            return null;
        }

        Map<String, List<String>> blockstates = modAssets.get("blockstates");
        if (!blockstates.containsKey(blockstateID)) {
            LOGGER.warn("Blockstate `{}` does not exist!", blockstateID);
            LOGGER.warn("Available blockstates: {}", blockstates);
            return null;
        }

        List<String> files = blockstates.get(blockstateID);
//        LOGGER.info("Found blockstate `{}` in {} files: {}", blockstateID, files.size(), files);

        return generatePaths(files, modID, "blockstates", blockstateID);
    }

    public static AssetPaths getModelPaths(String modId, String modelId) {
        modelId = modelId + ".json";

        if (!assetPaths.containsKey(modId)) {
            LOGGER.warn("Mod `{}` does not exist!", modId);
            return null;
        }

        Map<String, Map<String, List<String>>> modAssets = assetPaths.get(modId);
        if (!modAssets.containsKey("blockstates")) {
            LOGGER.warn("Mod `{}` does not have any models!", modId);
            return null;
        }

        Map<String, List<String>> models = modAssets.get("models");
        if (!models.containsKey(modelId)) {
            LOGGER.warn("Model `{}` does not exist!", modelId);
            LOGGER.warn("Available models: {}", models);
            return null;
        }

        List<String> files = models.get(modelId);
//        LOGGER.info("Found model `{}` in {} files: {}", modelId, files.size(), files);

        return generatePaths(files, modId, "models", modelId);
    }

    public static AssetPaths getTexturePaths(String modId, String textureId) {
        textureId = textureId + ".png";

        if (!assetPaths.containsKey(modId)) {
            LOGGER.warn("Mod `{}` does not exist!", modId);
            return null;
        }

        Map<String, Map<String, List<String>>> modAssets = assetPaths.get(modId);
        if (!modAssets.containsKey("textures")) {
            LOGGER.warn("Mod `{}` does not have any textures!", modId);
            return null;
        }

        Map<String, List<String>> textures = modAssets.get("textures");
        if (!textures.containsKey(textureId)) {
            LOGGER.warn("Texture `{}` does not exist!", textureId);
//            LOGGER.warn("Available textures: {}", textures);
            return null;
        }

        List<String> files = textures.get(textureId);
//        LOGGER.info("Found texture `{}` in {} files: {}", textureId, files.size(), files);

        return generatePaths(files, modId, "textures", textureId);
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
