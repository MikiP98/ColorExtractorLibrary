package io.github.mikip98.del;

import io.github.mikip98.del.api.BlockstatesAPI;
import io.github.mikip98.del.assetloading.AssetPathResolver;
import io.github.mikip98.del.enums.AVGTypes;
import io.github.mikip98.del.extractors.color.BlockModelColorExtractor;
import io.github.mikip98.del.extractors.color.BlockstateColorExtractor;
import io.github.mikip98.del.extractors.color.TextureColorExtractor;
import io.github.mikip98.del.structures.ColorReturn;
import io.github.mikip98.del.structures.SimplifiedProperty;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DataExtractionLibraryClient implements ClientModInitializer {
	public static final String MOD_NAME = "Data Extraction Library";
	public static final String MOD_ID = "data-extraction-library";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	@SuppressWarnings("rawtypes")
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		LOGGER.info("{} is initializing!", MOD_NAME);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(literal("data_extraction_library")
						.then(literal("cache")
								.then(literal("update_cache").executes(context -> {
									if (AssetPathResolver.updatePathCache()) {
										context.getSource().sendFeedback(Text.of("Cache updated!"));
									} else {
										context.getSource().sendFeedback(Text.of("Cache updated failed!\nCache is locked"));
									}
									return 0;
								}))
								.then(literal("clear_cache").executes(context -> {
									BlockstateColorExtractor.clearCache();
									context.getSource().sendFeedback(Text.of("Blockstate color cache cleared!"));

									BlockModelColorExtractor.clearCache();
									context.getSource().sendFeedback(Text.of("Block model color cache cleared!"));

									TextureColorExtractor.clearCache();
									context.getSource().sendFeedback(Text.of("Texture color cache cleared!"));

									AssetPathResolver.assetPaths.clear();
									context.getSource().sendFeedback(Text.of("Path cache cleared!"));
									return 0;
								}))
						)
						.then(literal("debug")
								.then(literal("log_light_blocks").executes(context -> {
									Map<String, Map<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> lightEmittingBlocks = BlockstatesAPI.getLightEmittingBlocksData();
									LOGGER.info("Light emitting blocks:");
									LOGGER.info("Mod count: {}", lightEmittingBlocks.size());
									for (Map.Entry<String, Map<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> entry : lightEmittingBlocks.entrySet()) {
										LOGGER.info("Mod: {}; With {} light emitting blocks:", entry.getKey(), entry.getValue().size());
										for (Map.Entry<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>> entry2 : entry.getValue().entrySet()) {
											LOGGER.info("  - Blockstate: {}; With {} light levels:", entry2.getKey(), entry2.getValue().size());
											for (Map.Entry<Byte, Set<Map<SimplifiedProperty, Comparable>>> entry3 : entry2.getValue().entrySet()) {
												LOGGER.info("    - Light level: {}; With {} property sets:", entry3.getKey(), entry3.getValue().size());
												for (Map<SimplifiedProperty, Comparable> propertySet : entry3.getValue()) {
													LOGGER.info("      - Property count: {}", propertySet.size());
													for (Map.Entry<SimplifiedProperty, Comparable> propertyValuePair : propertySet.entrySet()) {
														LOGGER.info("        - {}={}", propertyValuePair.getKey().name, propertyValuePair.getValue());
													}
												}
											}
										}
									}
									context.getSource().sendFeedback(Text.of("Done"));
									return 0;
								}))
								.then(literal("log_avg_colors_for_light_emitting_blocks").executes(context -> {
									Map<String, Map<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> lightEmittingBlocks = BlockstatesAPI.getLightEmittingBlocksData();
									for (Map.Entry<String, Map<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>>> entry : lightEmittingBlocks.entrySet()) {
										String modId = entry.getKey();
										LOGGER.info("- Mod: {}", modId);

										Map<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>> blockIds = entry.getValue();
										for (Map.Entry<String, Map<Byte, Set<Map<SimplifiedProperty, Comparable>>>> blockEntry : blockIds.entrySet()) {
											List<Map<SimplifiedProperty, Comparable>> requiredPropertySets = new ArrayList<>();
											for (Map.Entry<Byte, Set<Map<SimplifiedProperty, Comparable>>> lightLevelEntry : blockEntry.getValue().entrySet()) {
												Map<SimplifiedProperty, Comparable> requiredPropertySet = new HashMap<>();
												for (Map<SimplifiedProperty, Comparable> propertySet : lightLevelEntry.getValue()) {
													requiredPropertySet.putAll(propertySet);
												}
												requiredPropertySets.add(requiredPropertySet);
											}
											ColorReturn colorReturn = BlockstateColorExtractor.getAverageBlockstateColor(modId, blockEntry.getKey(), requiredPropertySets, 0.8f, AVGTypes.WEIGHTED_ARITHMETIC);
											if (colorReturn != null) {
												colorReturn.color_avg.multiply(255);
												colorReturn.color_avg.round();
												LOGGER.info("  - Blockstate: {}; Required properties: {}; Color: {}", blockEntry.getKey(), requiredPropertySets, colorReturn.color_avg);
											} else LOGGER.info("  - Blockstate: {}; Required properties: {}; Color: null", blockEntry.getKey(), requiredPropertySets);
										}
									}
									context.getSource().sendFeedback(Text.of("Done"));
									return 0;
								}))
								.then(literal("log_translucent_blocks").executes(context -> {
									Map<String, List<String>> translucentBlocks = BlockstatesAPI.getTranslucentBlockNames();

									LOGGER.info("Translucent blocks:");
									for (Map.Entry<String, List<String>> entry : translucentBlocks.entrySet()) {
										LOGGER.info("- mod: {}", entry.getKey());
										for (String blockstate : entry.getValue()) {
											LOGGER.info("  - blockstate: {}", blockstate);
										}
									}

									context.getSource().sendFeedback(Text.of("Done"));
									return 0;
								}))
								.then(literal("log_avg_colors_for_translucent_blocks").executes(context -> {
									Map<String, List<String>> translucentBlocks = BlockstatesAPI.getTranslucentBlockNames();

									LOGGER.info("Translucent blocks:");
									for (Map.Entry<String, List<String>> entry : translucentBlocks.entrySet()) {
										LOGGER.info("- mod: {}", entry.getKey());
										for (String blockstate : entry.getValue()) {
											ColorReturn colorReturn = BlockstateColorExtractor.getAverageBlockstateColor(entry.getKey(), blockstate, null, 0.8f, AVGTypes.WEIGHTED_ARITHMETIC);
											if (colorReturn != null) {
												colorReturn.color_avg.multiply(255);
												colorReturn.color_avg.round();
												LOGGER.info("  - blockstate: {}; color: {}", blockstate, colorReturn.color_avg);
											} else LOGGER.info("  - blockstate: {}; color: null", blockstate);
										}
									}

									context.getSource().sendFeedback(Text.of("Done"));
									return 0;
								}))
								.then(literal("log_non_full_blocks").executes(context -> {
									Map<String, Map<String, Double>> nonFullBlocks = BlockstatesAPI.getNonFullBlocks();

									LOGGER.info("Non full blocks:");
									for (Map.Entry<String, Map<String, Double>> entry : nonFullBlocks.entrySet()) {
										LOGGER.info("- mod: {}", entry.getKey());
										for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {
											LOGGER.info("  - blockstate: {}; volume: {}", entry2.getKey(), entry2.getValue());
										}
									}

									context.getSource().sendFeedback(Text.of("Done"));
									return 0;
								}))
						)
				)
		);
	}
}