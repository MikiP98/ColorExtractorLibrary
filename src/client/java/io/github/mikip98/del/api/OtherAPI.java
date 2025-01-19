package io.github.mikip98.del.api;

import io.github.mikip98.del.extractors.PropertyExtractor;
import io.github.mikip98.del.extractors.VolumeExtractor;
import io.github.mikip98.del.structures.SimplifiedProperty;
import net.minecraft.util.shape.VoxelShape;

import java.util.Map;

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

    /**
     * @return Map of property names to simplified properties
     */
    public static Map<String, SimplifiedProperty> getPropertyName2SimplifiedPropertyMap() {
        return PropertyExtractor.getPropertyName2SimplifiedPropertyMap();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
