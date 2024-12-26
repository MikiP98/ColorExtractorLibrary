package io.github.mikip98.cel.mixin.client;

import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(BlockRenderLayerMapImpl.class)
public class BRLMIMixin {
    @Shadow
    private static Map<Block, RenderLayer> blockRenderLayerMap;

    @Unique
    private static Map<Block, RenderLayer> getBlockRenderLayerMap() {
        return blockRenderLayerMap;
    }
}
