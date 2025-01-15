package io.github.mikip98.del.structures;

import java.util.List;
import java.util.Map;

public class VolumeData {
    public Map<String, Map<String, Double>> knownNonFullBlocksData;
    public Map<String, List<String>> unknownVolumeBlocks;

    public VolumeData(
            Map<String, Map<String, Double>> knownNonFullBlocksData,
            Map<String, List<String>> unknownVolumeBlocks
    ) {
        this.knownNonFullBlocksData = knownNonFullBlocksData;
        this.unknownVolumeBlocks = unknownVolumeBlocks;
    }
}
