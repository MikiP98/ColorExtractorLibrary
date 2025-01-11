package io.github.mikip98.del.api;

import io.github.mikip98.del.extractors.LightBlocksExtractor;
import io.github.mikip98.del.extractors.NonFullBlocksExtractor;
import io.github.mikip98.del.extractors.TranslucentBlocksExtractor;
import io.github.mikip98.del.structures.BlockstateWrapper;
import io.github.mikip98.del.structures.SimplifiedProperty;

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
    public static Map<String, Map<BlockstateWrapper, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> getLightEmittingBlocksData() {
        return LightBlocksExtractor.getLightEmittingBlocksData();
    }

    /**
     * @return Sorted by mod ids lists of blockstate ids marked as translucent
     */
    public static Map<String, List<String>> getTranslucentBlockNames() {
        return TranslucentBlocksExtractor.getTranslucentBlocks();
    }

    /**
     * @return Sorted, nested map of this format: modIds -> blockstateIds -> volume of the voxel shape
     */
    public static Map<String, Map<String, Double>> getNonFullBlocks() {
        return NonFullBlocksExtractor.getNonFullBlocks();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
