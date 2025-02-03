package io.github.mikip98.del.extractors;

import io.github.mikip98.del.structures.BlockstateWrapper;
import io.github.mikip98.del.structures.SimplifiedProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.mikip98.del.DataExtractionLibraryClient.LOGGER;

public class LightBlocksExtractor {
    @SuppressWarnings({"rawtypes", "unchecked", "UnusedReturnValue"})
    public static @NotNull Map<String, Map<BlockstateWrapper, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> getLightEmittingBlocksData() {
        // modId -> blockIds -> light levels -> property value pairs
        Map<String, Map<BlockstateWrapper, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> lightEmittingBlocks = new HashMap<>();

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

                // Remove all empty propertySets
                for (Set<Map<Property, Comparable>> propertySets : lightEmittingProperties.values()) {
                    propertySets.removeIf(Map::isEmpty);
                }

                // Test is any of the propertySets is empty, if so, throw exception
                for (Map.Entry<Byte, Set<Map<Property, Comparable>>> entry : lightEmittingProperties.entrySet()) {
                    Set<Map<Property, Comparable>> propertySets = entry.getValue();
                    for (Map<Property, Comparable> propertySet : propertySets) {
                        if (propertySet.isEmpty()) {
                            LOGGER.info("All property sets:");
                            for (Map.Entry<Byte, Set<Map<Property, Comparable>>> entry2 : lightEmittingProperties.entrySet()) {
                                byte lightLevel = entry2.getKey();
                                Set<Map<Property, Comparable>> propertySets2 = entry2.getValue();
                                LOGGER.info("  - Light level: {}; With {} property sets: {}", lightLevel, propertySets2.size(), propertySets2);
                            }
                            LOGGER.info("Active property sets: {}", propertySets);
                            throw new RuntimeException("PropertySet is empty for: " + block.getTranslationKey());
                        }
                    }
                }

                // Replace Property with SimplifiedProperty
                Map<Byte, Set<Map<SimplifiedProperty, Comparable>>> lightEmittingPropertiesNamed = new HashMap<>();
                for (Map.Entry<Byte, Set<Map<Property, Comparable>>> entry : lightEmittingProperties.entrySet()) {
                    byte lightLevel = entry.getKey();
                    Set<Map<Property, Comparable>> propertySets = entry.getValue();
                    lightEmittingPropertiesNamed.put(lightLevel, new HashSet<>());
                    for (Map<Property, Comparable> propertyValuePairs : propertySets) {
                        Map<SimplifiedProperty, Comparable> propertyValuePairsNamed = new HashMap<>();
                        for (Map.Entry<Property, Comparable> entry2 : propertyValuePairs.entrySet()) {
                            Property property = entry2.getKey();
                            Comparable value = entry2.getValue();

                            SimplifiedProperty simplifiedProperty = new SimplifiedProperty(
                                    property.getName(),
                                    new HashSet<Comparable>(property.getValues()),
                                    PropertyExtractor.getParserForComparable(value)
                            );
                            propertyValuePairsNamed.put(simplifiedProperty, value);
                        }
                        lightEmittingPropertiesNamed.get(lightLevel).add(propertyValuePairsNamed);
                    }
                }

                String[] parts = block.getTranslationKey().split("\\.", 3);
                String modId = parts[1];
                String blockstateId = parts[2];

                lightEmittingBlocks.putIfAbsent(modId, new HashMap<>());
                lightEmittingBlocks.get(modId).put(
                        new BlockstateWrapper(
                                blockstateId,
                                (byte) block.getDefaultState().getLuminance(),
                                VolumeExtractor.getVoxelShapeVolume(block.getDefaultState())
                        ),
                        lightEmittingPropertiesNamed
                );
            }
        }

        return lightEmittingBlocks;
    }

    @SuppressWarnings("rawtypes")
    protected static @NotNull Map<Byte, Set<Map<Property, Comparable>>> compressLightEmittingProperties(Map<Byte, Set<Map<Property, Comparable>>> lightEmittingProperties) {
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



    private static @NotNull List<Map<Property<?>, Comparable<?>>> generateCombinations(Collection<Property<?>> properties) {
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
