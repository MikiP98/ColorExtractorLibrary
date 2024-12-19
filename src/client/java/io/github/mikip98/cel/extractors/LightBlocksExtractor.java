package io.github.mikip98.cel.extractors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;

import java.util.*;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class LightBlocksExtractor {
    public static void getLightEmittingBlockstates() {
        for (Block block : Registries.BLOCK) {
            BlockState blockState = block.getDefaultState();

            Collection<Property<?>> properties = blockState.getProperties();



//            // Assuming blockState has a method to get its properties
//            Collection<Property<?>> properties = blockState.getProperties();

            // Generate all combinations of property values
            List<Map<Property<?>, Comparable<?>>> combinations = generateCombinations(properties);

            // Test each combination
            for (Map<Property<?>, Comparable<?>> combination : combinations) {
                BlockState testState = blockState;  // .withProperties(combination);
                for (Map.Entry<Property<?>, Comparable<?>> entry : combination.entrySet()) {

                    Property<?> property = entry.getKey();
                    @SuppressWarnings("rawtypes")
                    Comparable value = entry.getValue();

                    // Apply the property-value pair to the block state
                    testState = testState.with(property, value);
                }
                if (testState.getLuminance() > 0) {
                    LOGGER.info("Luminance > 0 for combination: {}", combination);
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
            List<Property<?>> properties, Map<Property<?>, Comparable<?>> currentCombination,
            List<Map<Property<?>, Comparable<?>>> result, int index
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
