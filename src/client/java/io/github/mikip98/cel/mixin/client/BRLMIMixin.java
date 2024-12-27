package io.github.mikip98.cel.mixin.client;

import io.github.mikip98.cel.util.Util;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

@Mixin(value = BlockRenderLayerMapImpl.class, remap = false)
public class BRLMIMixin {
    @Shadow
    private static Map<Block, RenderLayer> blockRenderLayerMap;

    @Inject(at = @At("RETURN"), method = "initialize")
    private static void initialize(BiConsumer<Block, RenderLayer> blockHandlerIn, BiConsumer<Fluid, RenderLayer> fluidHandlerIn, CallbackInfo ci) {
        Util.rawBlockRenderLayerMap = blockRenderLayerMap;

        Map<String, Map<String, List<String>>> remap = new HashMap<>();

        for (Map.Entry<Block, RenderLayer> entry : blockRenderLayerMap.entrySet()) {
            String[] blockTranslationParts = entry.getKey().getTranslationKey().split("\\.", 3);
            String modId = blockTranslationParts[1];
            String blockstateId = blockTranslationParts[2];

            RenderLayer layer = entry.getValue();
            remap
                    .computeIfAbsent(layer.toString().substring(11, layer.toString().split(":")[0].length()), k -> new HashMap<>())
                    .computeIfAbsent(modId, k -> new ArrayList<>()).add(blockstateId);
        }

        Util.renderLayerModBlockMap = remap;

//        LOGGER.info("BRLMIMixin: {}", remap);
//        for (Map.Entry<String, List<String>> entry : remap.entrySet()) {
//            LOGGER.info("  - Layer: {}", entry.getKey());
//            for (String s : entry.getValue()) {
//                LOGGER.info("    - Block: {}", s);
//            }
//        }
    }

//    @Unique
//    private static Map<Block, RenderLayer> getBlockRenderLayerMap() {
//        return blockRenderLayerMap;
//    }
}
