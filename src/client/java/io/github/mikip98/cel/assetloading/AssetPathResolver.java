package io.github.mikip98.cel.assetloading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetPathResolver {
    public Map<String, List<String>> assetPaths = new HashMap<>();

    public static boolean pathsCached = false;

    public static void cachePathsIfNotCached() {
        if (!pathsCached) {
            throw new RuntimeException("Paths not cached!");

            pathsCached = true;
        }
    }

    public static void updatePathCache() {
        throw new RuntimeException("Paths not cached!");
    }
}
