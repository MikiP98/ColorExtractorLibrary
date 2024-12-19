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
    public static Map<String, Map<String, List<String>>> assetPaths = new HashMap<>();
    public static boolean arePathsCached = false;
    public static List<Short> pathsLocks = new ArrayList<>();

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

            int size = 0;
            LOGGER.info("Asset path cache updated!");
            LOGGER.info("Found `{}` mods:", assetPaths.size());
            for (Map.Entry<String, Map<String, List<String>>> entry : assetPaths.entrySet()) {
                size += entry.getKey().getBytes().length;
                LOGGER.info("  - ModID: {}", entry.getKey());
                for (Map.Entry<String, List<String>> assetEntry : entry.getValue().entrySet()) {
                    size += assetEntry.getKey().getBytes().length;
                    LOGGER.info("    - Asset entry: {}; Length: {}", assetEntry.getKey(), assetEntry.getValue().size());
                    for (String asset : assetEntry.getValue()) {
                        size += asset.getBytes().length;
                    }
                }
            }
            LOGGER.info("Bytes: {}; Kilobytes: {}; Megabytes: {}", size,
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
    public static void handleZipOrJar(File file, HashSet<String> cachedAssetTypes) {
        try (ZipFile zipFile = new ZipFile(file)) {

            String lastModID = null;
            Map<String, List<String>> entryTypes = new HashMap<>();

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().startsWith("assets/") && !entry.isDirectory()) {
                    String[] parts = entry.getName().split("/");
                    String currentModID = parts[1];

                    if (!Objects.equals(lastModID, currentModID)) {
                        if (lastModID != null) {
                            assetPaths.put(lastModID, entryTypes);
                        }
                        lastModID = currentModID;
                        entryTypes.clear();
                    }

                    String entryType = parts[2];
                    if (cachedAssetTypes.contains(entryType)) {
                        String rest = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));
                        entryTypes.computeIfAbsent(entryType, k -> new ArrayList<>()).add(rest);
                    }
                }
            }
            assetPaths.put(lastModID, entryTypes);
        } catch (ZipException e) {
            LOGGER.error("Failed to handle zip or jar (ZIP)!;\n{}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Failed to handle zip or jar (IO)!;\n{}", e.getMessage());
        }
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
}
