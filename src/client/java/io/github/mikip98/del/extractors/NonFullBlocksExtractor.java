package io.github.mikip98.del.extractors;

import io.github.mikip98.del.structures.VolumeData;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonFullBlocksExtractor {

    @SuppressWarnings("deprecation")
    public static @NotNull VolumeData getNonFullBlocks() {
        Map<String, Map<String, Double>> nonFullBlocks = new HashMap<>();
        Map<String, List<String>> unknownVolumeBlocks = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            String[] blockTranslationParts = block.getTranslationKey().split("\\.");
            String modId = blockTranslationParts[1];
            String blockstateId = blockTranslationParts[2];
            try {
                VoxelShape voxelShape = block.getCollisionShape(block.getDefaultState(), null, null, null);
                if (!Block.isShapeFullCube(voxelShape)) {
                    nonFullBlocks
                            .computeIfAbsent(modId, k -> new HashMap<>())
                            .computeIfAbsent(blockstateId, k -> VolumeExtractor.getVoxelShapeVolume(voxelShape));
                }
            } catch (NullPointerException ignored) {
                unknownVolumeBlocks.computeIfAbsent(modId, k -> new ArrayList<>()).add(blockstateId);
            }
        }
        return new VolumeData(nonFullBlocks, unknownVolumeBlocks);
    }
}
