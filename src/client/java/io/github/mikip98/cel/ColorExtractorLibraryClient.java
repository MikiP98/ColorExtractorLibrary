package io.github.mikip98.cel;

import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.extractors.LightBlocksExtractor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ColorExtractorLibraryClient implements ClientModInitializer {
	public static final String MOD_NAME = "Color Extractor Library";
	public static final String MOD_ID = "color-extractor-library";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		LOGGER.info("{} is initializing!", MOD_NAME);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(literal("color_extractor_library")
						.then(literal("update_cache").executes(context -> {
							AssetPathResolver.updatePathCache();
							return 0;
						}))
						.then(literal("clear_cache").executes(context -> {
							AssetPathResolver.clearPathCache();
							return 0;
						}))
						.then(literal("log_light_blocks").executes(context -> {
							LightBlocksExtractor.getLightEmittingBlockstates();
							return 0;
						}))
				)
		);
	}
}