package io.github.mikip98.cel;

import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.extractors.BlockstateColorExtractor;
import io.github.mikip98.cel.extractors.LightBlocksExtractor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ColorExtractorLibraryClient implements ClientModInitializer {
	public static final String MOD_NAME = "Color Extractor Library";
	public static final String MOD_ID = "color-extractor-library";

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
				dispatcher.register(literal("color_extractor_library")
						.then(literal("cache")
								.then(literal("update_cache").executes(context -> {
									AssetPathResolver.updatePathCache();
									return 0;
								}))
								.then(literal("clear_cache").executes(context -> {
									AssetPathResolver.clearPathCache();
									return 0;
								}))
						)
						.then(literal("debug")
								.then(literal("log_light_blocks").executes(context -> {
									Map<String, Map<String, Map<Byte, Set<Map<String, Comparable>>>>> lightEmittingBlocks = LightBlocksExtractor.getLightEmittingBlocksData();
									LOGGER.info("Light emitting blocks:");
									LOGGER.info("Mod count: {}", lightEmittingBlocks.size());
									for (Map.Entry<String, Map<String, Map<Byte, Set<Map<String, Comparable>>>>> entry : lightEmittingBlocks.entrySet()) {
										LOGGER.info("Mod: {}; With {} light emitting blocks:", entry.getKey(), entry.getValue().size());
										for (Map.Entry<String, Map<Byte, Set<Map<String, Comparable>>>> entry2 : entry.getValue().entrySet()) {
											LOGGER.info("  - Blockstate: {}; With {} light levels:", entry2.getKey(), entry2.getValue().size());
											for (Map.Entry<Byte, Set<Map<String, Comparable>>> entry3 : entry2.getValue().entrySet()) {
												LOGGER.info("    - Light level: {}; With {} property sets:", entry3.getKey(), entry3.getValue().size());
												for (Map<String, Comparable> propertySet : entry3.getValue()) {
													LOGGER.info("      - Property count: {}", propertySet.size());
													for (Map.Entry<String, Comparable> propertyValuePair : propertySet.entrySet()) {
														LOGGER.info("        - {}={}", propertyValuePair.getKey(), propertyValuePair.getValue());
													}
												}
											}
										}
									}
									return 0;
								}))
								.then(literal("log_blockstate_color_extrusion").executes(context -> {
									Map<String, Map<String, Map<Byte, Set<Map<String, Comparable>>>>> lightEmittingBlocks = LightBlocksExtractor.getLightEmittingBlocksData();
									for (Map.Entry<String, Map<String, Map<Byte, Set<Map<String, Comparable>>>>> entry : lightEmittingBlocks.entrySet()) {
										String modId = entry.getKey();
										Map<String, Map<Byte, Set<Map<String, Comparable>>>> blockIds = entry.getValue();
										for (Map.Entry<String, Map<Byte, Set<Map<String, Comparable>>>> blockEntry : blockIds.entrySet()) {
											BlockstateColorExtractor.getAverageBlockstateColor(modId, blockEntry.getKey(), 0.5f, AVGTypes.WEIGHTED_ARITHMETIC);
										}
									}
									return 0;
								}))
						)
				)
		);
	}
}