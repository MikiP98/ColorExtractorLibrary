package io.github.mikip98.cel.extractors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.*;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class LightBlocksExtractor {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void getLightEmittingBlockstates() {
        for (Block block : Registries.BLOCK) {
            BlockState blockState = block.getDefaultState();

            Collection<Property<? extends Comparable<?>>> properties = blockState.getProperties();

            // Generate all combinations of property values
            List<Map<Property<?>, Comparable<?>>> combinations = generateCombinations(properties);

            // Test each combination
            for (Map<Property<?>, Comparable<?>> combination : combinations) {
                BlockState testState = blockState;

                for (Map.Entry<Property<?>, Comparable<?>> entry : combination.entrySet()) {
                    Property<?> property = entry.getKey();

                    if (property instanceof BooleanProperty new_property) {
                        testState = testState.with(new_property, (Boolean) entry.getValue());

                    } else if (property instanceof DirectionProperty new_property) {
                        testState = testState.with(new_property, (Direction) entry.getValue());

                    } else if (property instanceof EnumProperty new_property) {
                        testState = testState.with(new_property, (Enum) entry.getValue());

                    } else if (property instanceof IntProperty new_property) {
                        testState = testState.with(new_property, (Integer) entry.getValue());

                    } else {
                        LOGGER.warn("Unsupported property type: {}", property.getClass());
                    }
                }

                if (testState.getLuminance() > 0) {
                    LOGGER.info("Luminance `{}` for combination: {}", testState.getLuminance(), combination);
                }
            }
        }
    }



    private static List<Map<Property<?>, Comparable<?>>> generateCombinations(Collection<Property<?>> properties) {
        List<Map<Property<?>, Comparable<?>>> result = new ArrayList<>();
        generateCombinationsRecursive(new ArrayList<>(properties), new HashMap<>(), result, 0);
        return result;
    }

    private static void generateCombinationsRecursive(
            List<Property<?>> properties,
            Map<Property<?>, Comparable<?>> currentCombination,
            List<Map<Property<?>, Comparable<?>>> result,
            int index
    ) {
        if (index == properties.size()) {
            result.add(new HashMap<>(currentCombination));
            return;
        }

        Property<?> property = properties.get(index);
        for (Comparable<?> value : property.getValues()) {
            currentCombination.put(property, value);
            generateCombinationsRecursive(properties, currentCombination, result, index + 1);
            currentCombination.remove(property); // Backtrack
        }
    }
}
