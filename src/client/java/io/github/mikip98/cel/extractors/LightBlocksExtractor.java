package io.github.mikip98.cel.extractors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.*;

import java.util.*;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class LightBlocksExtractor {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Map<String, Map<Byte, Set<Map<Property, Comparable>>>>> getLightEmittingBlockstates() {
        // modId -> blockIds -> light levels -> property value pairs
        Map<String, Map<String, Map<Byte, Set<Map<Property, Comparable>>>>> lightEmittingBlocks = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            Map<Byte, Set<Map<Property, Comparable>>> lightEmittingProperties = new HashMap<>();

            BlockState blockState = block.getDefaultState();

            Collection<Property<?>> properties = blockState.getProperties();

            // Generate all combinations of property values
            List<Map<Property<?>, Comparable<?>>> combinations = generateCombinations(properties);

            // Test each combination
            for (Map<Property<?>, Comparable<?>> combination : combinations) {
                BlockState testState = blockState;

                HashMap<Property, Comparable> propertyValuePairs = new HashMap<>();

                for (Map.Entry<Property<?>, Comparable<?>> entry : combination.entrySet()) {
                    Property property = entry.getKey();
                    Comparable value = entry.getValue();

                    testState = testState.with(property, value);
                    propertyValuePairs.put(property, value);
                }

                byte luminance = (byte) testState.getLuminance();
                if (luminance > 0) {
                    lightEmittingProperties.putIfAbsent(luminance, new HashSet<>());

                    lightEmittingProperties.get(luminance).add(propertyValuePairs);
                }
            }

            if (!lightEmittingProperties.isEmpty()) {

                // Compress the propertySets
                lightEmittingProperties = compressLightEmittingProperties(lightEmittingProperties);

                String[] parts = block.getTranslationKey().split("\\.");
                String modId = parts[1];
                String blockstateId = parts[2];

                lightEmittingBlocks.putIfAbsent(modId, new HashMap<>());
                lightEmittingBlocks.get(modId).put(blockstateId, lightEmittingProperties);
            }
        }

        LOGGER.info("Light emitting blocks:");
        LOGGER.info("Mod count: {}", lightEmittingBlocks.size());
        for (Map.Entry<String, Map<String, Map<Byte, Set<Map<Property, Comparable>>>>> entry : lightEmittingBlocks.entrySet()) {
            LOGGER.info("Mod: {}; With {} light emitting blocks:", entry.getKey(), entry.getValue().size());
            for (Map.Entry<String, Map<Byte, Set<Map<Property, Comparable>>>> entry2 : entry.getValue().entrySet()) {
                LOGGER.info("  - Blockstate: {}; With {} light levels:", entry2.getKey(), entry2.getValue().size());
                for (Map.Entry<Byte, Set<Map<Property, Comparable>>> entry3 : entry2.getValue().entrySet()) {
                    LOGGER.info("    - Light level: {}; With {} property sets:", entry3.getKey(), entry3.getValue().size());
                    for (Map<Property, Comparable> propertySet : entry3.getValue()) {
                        LOGGER.info("      - Property count: {}", propertySet.size());
                        for (Map.Entry<Property, Comparable> propertyValuePair : propertySet.entrySet()) {
                            LOGGER.info("        - {}={}", propertyValuePair.getKey().getName(), propertyValuePair.getValue());
                        }
                    }
                }
            }
        }

        return lightEmittingBlocks;
    }

    @SuppressWarnings("rawtypes")
    public static Map<Byte, Set<Map<Property, Comparable>>> compressLightEmittingProperties(Map<Byte, Set<Map<Property, Comparable>>> lightEmittingProperties) {
        Map<Byte, Set<Map<Property, Comparable>>> compressedLightEmittingProperties = new HashMap<>();

        for (Map.Entry<Byte, Set<Map<Property, Comparable>>> entry : lightEmittingProperties.entrySet()) {
            byte lightLevel = entry.getKey();
            compressedLightEmittingProperties.putIfAbsent(lightLevel, new HashSet<>());

            Map<Property, Set<Comparable>> usedValues = new HashMap<>();

            for (Map<Property, Comparable> propertySet : entry.getValue()) {
                for (Map.Entry<Property, Comparable> propertyValuePair : propertySet.entrySet()) {
                    Property property = propertyValuePair.getKey();
                    Comparable value = propertyValuePair.getValue();

                    usedValues.computeIfAbsent(property, k -> new HashSet<>()).add(value);
                }
            }

            for (Map<Property, Comparable> propertySet : entry.getValue()) {
                Map<Property, Comparable> compressedPropertySet = new HashMap<>();

                for (Map.Entry<Property, Comparable> propertyValuePair : propertySet.entrySet()) {
                    Property property = propertyValuePair.getKey();
                    Comparable value = propertyValuePair.getValue();

                    if (property.getValues().size() != usedValues.get(property).size()) {
                        compressedPropertySet.put(property, value);
                    }
                }

                compressedLightEmittingProperties.get(lightLevel).add(compressedPropertySet);
            }
        }

        return compressedLightEmittingProperties;
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
