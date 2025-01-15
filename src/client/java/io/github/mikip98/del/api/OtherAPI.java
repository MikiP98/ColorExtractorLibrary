package io.github.mikip98.del.api;

import io.github.mikip98.del.extractors.VolumeExtractor;
import net.minecraft.util.shape.VoxelShape;

// This is the one of the classed that mods using this lib should use/one of the ones that guarantee stability
@SuppressWarnings("unused")
public class OtherAPI {

    // ------------------------------------------------------------------------
    // --------------------------------- OTHER --------------------------------
    // ------------------------------------------------------------------------

    /**
     * Function for getting the approximate volume of a voxel shape
     * @param voxelShape The voxel shape, presumably from a block
     * @return The approximate volume or null if the volume cannot be calculated
     */
    public static Double getVoxelShapeVolume(VoxelShape voxelShape) {
        return VolumeExtractor.getVoxelShapeVolume(voxelShape);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
