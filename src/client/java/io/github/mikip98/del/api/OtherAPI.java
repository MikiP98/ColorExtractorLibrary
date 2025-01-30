package io.github.mikip98.del.api;

import io.github.mikip98.del.extractors.PropertyExtractor;
import io.github.mikip98.del.extractors.VolumeExtractor;
import io.github.mikip98.del.structures.EProperty;
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
     * @return Map of property names to EProperties, being either Quantum or Simple properties.
     *         If a given name has only one possible property it wil be a Simple property.
     *         If a given name has multiple possible properties it will be a Quantum property
     */
    public static Map<String, EProperty> getPropertyName2EPropertyMap() {
        return PropertyExtractor.getPropertyName2EPropertyMap();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
