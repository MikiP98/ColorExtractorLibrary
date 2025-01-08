package io.github.mikip98.del.extractors;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.shape.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class NonFullBlocksExtractor {

    @SuppressWarnings("deprecation")
    public static Map<String, Map<String, Double>> getNonFullBlocks() {
        Map<String, Map<String, Double>> nonFullBlocks = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            try {
                VoxelShape voxelShape = block.getCollisionShape(block.getDefaultState(), null, null, null);
                if (!Block.isShapeFullCube(voxelShape)) {
                    String[] blockTranslationParts = block.getTranslationKey().split("\\.");
                    String modId = blockTranslationParts[1];
                    String blockstateId = blockTranslationParts[2];

                    nonFullBlocks.computeIfAbsent(modId, k -> new HashMap<>()).computeIfAbsent(blockstateId, k -> getVoxelShapeVolume(voxelShape));
                }
            } catch (NullPointerException ignored) {}
        }
        return nonFullBlocks;
    }

    public static Double getVoxelShapeVolume(VoxelShape voxelShape) {
        AtomicReference<Double> volume = new AtomicReference<>(0.0);
        voxelShape.forEachBox((x1, y1, z1, x2, y2, z2) -> volume.updateAndGet(v -> v + (x2 - x1) * (y2 - y1) * (z2 - z1)));
        return volume.get();
    }
}
