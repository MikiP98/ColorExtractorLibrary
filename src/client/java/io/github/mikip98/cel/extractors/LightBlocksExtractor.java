package io.github.mikip98.cel.extractors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.*;
import net.minecraft.util.math.Direction;

import java.util.*;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class LightBlocksExtractor {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static HashMap<String, HashMap<String, HashMap<Byte, HashSet<HashMap<Property<? extends Comparable<?>>, ?>>>>> getLightEmittingBlockstates() {
        // modId -> list of blockIds -> list of blockStates
        HashMap<
                String, // modId
                HashMap<
                        String, // blockstateId
                        HashMap<
                                Byte, // light level
                                HashSet<
                                        HashMap<Property<? extends Comparable<?>>, ?>
                                        >
                                >
                        >
                > lightEmittingBlocks = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            HashMap<
                    Byte, // light level
                    HashSet<
                            HashMap<Property<? extends Comparable<?>>, ?>
                            >
                    > lightEmittingProperties = new HashMap<>();

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
                        // How to make it universal?
                        LOGGER.warn("BLOCK LOOP: Unsupported property type: {}", property.getClass());
                    }
                }

                byte luminance = (byte) testState.getLuminance();
                if (luminance > 0) {
//                    LOGGER.info("Luminance `{}` for combination: {}", testState.getLuminance(), combination);
                    lightEmittingProperties.putIfAbsent(luminance, new HashSet<>());

                    HashMap<Property<? extends Comparable<?>>, Comparable<?>> combinationValueSet = new HashMap<>();
                    for (Property<?> property : combination.keySet()) {
                        combinationValueSet.put(property, combination.get(property));
                    }

                    lightEmittingProperties.get(luminance).add(combinationValueSet);
                    // ComputeIfAbsent ?
                }
            }

            if (!lightEmittingProperties.isEmpty()) {

                // Compress the propertySets
                // If for the current light level, a single property goes through all possible values while the others are the same, remove them

                HashMap<
                        Byte, // light level
                        HashSet<
                                HashMap<Property<? extends Comparable<?>>, ?>
                                >
                        > compressedLightEmittingProperties = new HashMap<>();

                for (Map.Entry<Byte, HashSet<HashMap<Property<? extends Comparable<?>>, ?>>> entry : lightEmittingProperties.entrySet()) {
                    byte lightLevel = entry.getKey();
                    compressedLightEmittingProperties.putIfAbsent(lightLevel, new HashSet<>());

                    HashMap<BooleanProperty, Set<Boolean>> booleanUsedValues = new HashMap<>();
                    HashMap<DirectionProperty, Set<Direction>> directionUsedValues = new HashMap<>();
                    HashMap<EnumProperty<?>, Set<Enum<?>>> enumUsedValues = new HashMap<>();
                    HashMap<IntProperty, Set<Integer>> intUsedValues = new HashMap<>();

                    for (HashMap<Property<? extends Comparable<?>>, ?> propertySet : entry.getValue()) {
                        for (Map.Entry<Property<? extends Comparable<?>>, ?> propertyValuePair : propertySet.entrySet()) {
                            if (propertyValuePair.getKey() instanceof BooleanProperty new_property) {
                                booleanUsedValues.computeIfAbsent(new_property, k -> new HashSet<>()).add((Boolean) propertyValuePair.getValue());

                            } else if (propertyValuePair.getKey() instanceof DirectionProperty new_property) {
                                directionUsedValues.computeIfAbsent(new_property, k -> new HashSet<>()).add((Direction) propertyValuePair.getValue());

                            } else if (propertyValuePair.getKey() instanceof EnumProperty new_property) {
                                enumUsedValues.computeIfAbsent(new_property, k -> new HashSet<>()).add((Enum) propertyValuePair.getValue());

                            } else if (propertyValuePair.getKey() instanceof IntProperty new_property) {
                                intUsedValues.computeIfAbsent(new_property, k -> new HashSet<>()).add((Integer) propertyValuePair.getValue());

                            } else {
                                // How to make it universal?
                                LOGGER.warn("VALUES USED CHECK: Unsupported property type: {}", propertyValuePair.getKey().getClass());
                            }
                        }
                    }

                    for (HashMap<Property<? extends Comparable<?>>, ?> propertySet : entry.getValue()) {
                        HashMap<Property<? extends Comparable<?>>, Set<?>> compressedPropertySet = new HashMap<>();

                        for (Map.Entry<Property<? extends Comparable<?>>, ?> propertyValuePair : propertySet.entrySet()) {
                            if (propertyValuePair.getKey() instanceof BooleanProperty new_property) {
                                if (new_property.getValues().size() != booleanUsedValues.get(new_property).size()) {
                                    compressedPropertySet.put(new_property, booleanUsedValues.get(new_property));
                                }

                            } else if (propertyValuePair.getKey() instanceof DirectionProperty new_property) {
                                if (new_property.getValues().size() != directionUsedValues.get(new_property).size()) {
                                    compressedPropertySet.put(new_property, directionUsedValues.get(new_property));
                                }

                            } else if (propertyValuePair.getKey() instanceof EnumProperty new_property) {
                                if (new_property.getValues().size() != enumUsedValues.get(new_property).size()) {
                                    compressedPropertySet.put(new_property, enumUsedValues.get(new_property));
                                }

                            } else if (propertyValuePair.getKey() instanceof IntProperty new_property) {
                                if (new_property.getValues().size() != intUsedValues.get(new_property).size()) {
                                    compressedPropertySet.put(new_property, intUsedValues.get(new_property));
                                }

                            } else {
                                // How to make it universal?
                                LOGGER.warn("COMPRESSION: Unsupported property type: {}", propertyValuePair.getKey().getClass());
                            }
                        }

                        compressedLightEmittingProperties.get(lightLevel).add(compressedPropertySet);
                    }
                }

                lightEmittingProperties = compressedLightEmittingProperties;

                String[] parts = block.getTranslationKey().split("\\.");
//                LOGGER.info("Block: {}", block.getTranslationKey());
                String modId = parts[1];
                String blockstateId = parts[2];

                lightEmittingBlocks.putIfAbsent(modId, new HashMap<>());
                lightEmittingBlocks.get(modId).put(blockstateId, lightEmittingProperties);
            }
        }

        LOGGER.info("Light emitting blocks:");
        LOGGER.info("Mod count: {}", lightEmittingBlocks.size());
        for (Map.Entry<String, HashMap<String, HashMap<Byte, HashSet<HashMap<Property<? extends Comparable<?>>, ?>>>>> entry : lightEmittingBlocks.entrySet()) {
            LOGGER.info("Mod: {}; With {} light emitting blocks:", entry.getKey(), entry.getValue().size());
            for (Map.Entry<String, HashMap<Byte, HashSet<HashMap<Property<? extends Comparable<?>>, ?>>>> entry2 : entry.getValue().entrySet()) {
                LOGGER.info("  - Blockstate: {}; With {} light levels:", entry2.getKey(), entry2.getValue().size());
                for (Map.Entry<Byte, HashSet<HashMap<Property<? extends Comparable<?>>, ?>>> entry3 : entry2.getValue().entrySet()) {
                    LOGGER.info("    - Light level: {}; With {} property sets:", entry3.getKey(), entry3.getValue().size());
                    for (HashMap<Property<? extends Comparable<?>>, ?> propertySet : entry3.getValue()) {
                        for (Map.Entry<Property<? extends Comparable<?>>, ?> propertyValuePair : propertySet.entrySet()) {
                            LOGGER.info("      - Property: {}={}", propertyValuePair.getKey().getName(), propertyValuePair.getValue().toString().substring(1, propertyValuePair.getValue().toString().length() - 1));
                        }
                    }
                }
            }
        }

        return lightEmittingBlocks;
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
