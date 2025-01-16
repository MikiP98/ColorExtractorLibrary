package io.github.mikip98.del.extractors;

import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class VolumeExtractor {

    public static Double getVoxelShapeVolume(BlockState blockState) {
        try {
            return getVoxelShapeVolume(blockState.getCollisionShape(null, null));
        } catch (NullPointerException ignored) {
            return null;
        }
    }
    public static Double getVoxelShapeVolume(VoxelShape voxelShape) {
        try {
            AtomicReference<Double> volume = new AtomicReference<>(0.0);
            voxelShape.forEachBox((x1, y1, z1, x2, y2, z2) -> volume.updateAndGet(v -> v + (x2 - x1) * (y2 - y1) * (z2 - z1)));
            return volume.get();
        } catch (NullPointerException ignored) {
            return null;
        }
    }
}
