package io.github.mikip98.del.extractors;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.shape.VoxelShape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonFullBlocksExtractor {

    @SuppressWarnings("deprecation")
    public static Map<String, Map<String, Double>> getNonFullBlocks() {
        Map<String, Map<String, Double>> nonFullBlocks = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            try {
                VoxelShape voxelShape = block.getCollisionShape(block.getDefaultState(), null, null, null);
                if (!Block.isShapeFullCube(voxelShape)) {
                    String[] blockTranslationParts = block.getTranslationKey().split(":");
                    String modId = blockTranslationParts[0];
                    String blockstateId = blockTranslationParts[1];

                    nonFullBlocks.computeIfAbsent(modId, k -> new HashMap<>()).computeIfAbsent(blockstateId, k -> getVoxelShapeVolume(voxelShape));
                }
            } catch (NullPointerException ignored) {}
        }
        return new HashMap<>();
    }

    public static Double getVoxelShapeVolume(VoxelShape voxelShape) {
        return null;
    }
}
