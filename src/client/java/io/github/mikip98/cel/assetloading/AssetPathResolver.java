package io.github.mikip98.cel.assetloading;

import com.sun.jna.platform.win32.WinDef;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class AssetPathResolver {
    public static Map<String, List<String>> assetPaths = new HashMap<>();
    public static boolean arePathsCached = false;
    public static boolean arePathsLocked = false;
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
        if (!arePathsLocked) {
            clearPathCache();

            // Get the mods directory path
            Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");

            // Go through every `.jar` and `.zip` file in the `mods` directory
            try (Stream<Path> paths = Files.list(modsFolder)) {
                paths.filter(file -> file.toString().endsWith(".jar") || file.toString().endsWith(".zip")).forEach(modPath -> {
                    if (modPath.toString().endsWith(".jar") || modPath.toString().endsWith(".zip")) {
                        try (ZipFile zipFile = new ZipFile(modPath.toFile())) {

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
                                            LOGGER.info("Found mod ID: {}", lastModID);
                                            LOGGER.info("With entries: {}", entryTypes);
                                        }
                                        lastModID = currentModID;
                                        entryTypes.clear();
                                    }

                                    String entryType = parts[2];
                                    String rest = String.join("/", Arrays.copyOfRange(parts, 3, parts.length));
                                    entryTypes.computeIfAbsent(entryType, k -> new ArrayList<>()).add(rest);
                                }
                            }
                            LOGGER.info("Found mod ID: {}", lastModID);
                            LOGGER.info("With entries: {}", entryTypes);
                        } catch (ZipException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    LOGGER.info("Checked file: {}", modPath.getFileName());
                });
            } catch (IOException e) {
                // LOG the error
                LOGGER.error("Failed to update asset path cache!");
                isUpdateQueued = true;
                // TODO: Lock the paths for 60s
                return false;
            }
            LOGGER.info("Asset path cache updated!");
            LOGGER.info("Asset path cache size: {}", assetPaths.size());
            LOGGER.info(assetPaths.toString());

            arePathsCached = true;
            return true;
        } else {
            // LOG the error
            isUpdateQueued = queueTheUpdate;
            return false;
        }
    }

    public static boolean clearPathCache() {
        return clearPathCache(false);
    }
    public static boolean clearPathCache(boolean queueTheClear) {
        if (!arePathsLocked) {
            arePathsCached = false;
            assetPaths.clear();
            return true;
        } else {
            // LOG the error
            isClearQueued = queueTheClear;
            return false;
        }
    }
}
