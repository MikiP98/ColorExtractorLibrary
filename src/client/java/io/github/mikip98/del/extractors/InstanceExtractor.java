package io.github.mikip98.del.extractors;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InstanceExtractor {

    public static @NotNull Map<String, List<String>> getChildBlockstateNamesOfClass(Class<?> clazz) {
        Map<String, List<String>> blockstateChildrenOfClass = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            if (clazz.isInstance(block)) {
                String[] blockTranslationParts = block.getTranslationKey().split("\\.");
                String modId = blockTranslationParts[1];
                String blockstateId = blockTranslationParts[2];
                blockstateChildrenOfClass
                        .computeIfAbsent(modId, k -> new ArrayList<>())
                        .add(blockstateId);
            }
        }

        return blockstateChildrenOfClass;
    }

    public static @NotNull Map<Class<?>, Map<String, List<String>>> getChildBlockstateNamesOfClasses(Set<Class<?>> classes) {
        Map<Class<?>, Map<String, List<String>>> blockstateChildrenOfClasses = new HashMap<>();
        for (Class<?> clazz : classes) {
            blockstateChildrenOfClasses.put(clazz, new HashMap<>());
        }

        for (Block block : Registries.BLOCK) {
            for (Class<?> clazz : classes) {
                if (clazz.isInstance(block)) {
                    String[] blockTranslationParts = block.getTranslationKey().split("\\.");
                    String modId = blockTranslationParts[1];
                    String blockstateId = blockTranslationParts[2];
                    blockstateChildrenOfClasses.get(clazz)
                            .computeIfAbsent(modId, k -> new ArrayList<>())
                            .add(blockstateId);
                }
            }
        }

        return blockstateChildrenOfClasses;
    }
}
