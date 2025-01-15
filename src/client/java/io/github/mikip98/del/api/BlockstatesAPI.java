package io.github.mikip98.del.api;

import io.github.mikip98.del.extractors.InstanceExtractor;
import io.github.mikip98.del.extractors.LightBlocksExtractor;
import io.github.mikip98.del.extractors.NonFullBlocksExtractor;
import io.github.mikip98.del.extractors.TranslucentBlocksExtractor;
import io.github.mikip98.del.structures.BlockstateWrapper;
import io.github.mikip98.del.structures.SimplifiedProperty;
import io.github.mikip98.del.structures.VolumeData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

// This is the one of the classed that mods using this lib should use/one of the ones that guarantee stability
@SuppressWarnings("unused")
public class BlockstatesAPI {

    // ------------------------------------------------------------------------
    // ----------------------------- BLOCKSTATES ------------------------------
    // ------------------------------------------------------------------------

    /**
     * @return Sorted, nested map of this format: modIds -> blockstateIds -> light levels -> property value pairs
     */
    @SuppressWarnings("rawtypes")
    public static @NotNull Map<String, Map<BlockstateWrapper, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> getLightEmittingBlocksData() {
        return LightBlocksExtractor.getLightEmittingBlocksData();
    }

    /**
     * @return Sorted by mod ids lists of blockstate ids marked as translucent
     */
    public static @NotNull Map<String, List<String>> getTranslucentBlockNames() {
        return TranslucentBlocksExtractor.getTranslucentBlocks();
    }

    /**
     * @return 'VolumeData' object containing 'Map<String, Map<String, Double>> knownNonFullBlocksData' and 'Map<String, List<String>> unknownVolumeBlocks'
     */
    public static @NotNull VolumeData getNonFullBlocks() {
        return NonFullBlocksExtractor.getNonFullBlocks();
    }

    /**
     * Fetches all blockstate names that are children or instances of a given class
     * @param clazz The class that block needs to be an instance/child of
     * @return Map: modId -> list of blockstateIds
     */
    public static @NotNull Map<String, List<String>> getChildBlockstatesOfClass(Class<?> clazz) {
        return InstanceExtractor.getChildBlockstateNamesOfClass(clazz);
    }

    /**
     * Fetches all blockstate names that are children or instances of a given set of classes
     * @param classes Set of classes that block needs to be an instance/child of
     * @return Nested map: class -> modId -> list of blockstateIds
     */
    public static @NotNull Map<Class<?>, Map<String, List<String>>> getChildBlockstatesOfClasses(Set<Class<?>> classes) {
        return InstanceExtractor.getChildBlockstateNamesOfClasses(classes);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
