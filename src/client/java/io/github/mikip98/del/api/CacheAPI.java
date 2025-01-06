package io.github.mikip98.del.api;

import io.github.mikip98.del.assetloading.AssetPathResolver;

// This is the one of the classed that mods using this lib should use/one of the ones that guarantee stability
@SuppressWarnings("unused")
public class CacheAPI {

    // ------------------------------------------------------------------------
    // --------------------------------- SETUP --------------------------------
    // ------------------------------------------------------------------------

    public static void cachePathsIfNotCached() {
        AssetPathResolver.cachePathsIfNotCached();
    }

    public static boolean updatePathCache() { return AssetPathResolver.updatePathCache(); }

//    public static boolean clearPathCache() {
//        return AssetPathResolver.clearPathCache();
//    }



    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
