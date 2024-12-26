package io.github.mikip98.cel.extractors;

import io.github.mikip98.cel.mixin.client.BRLMIMixin;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TranslucentBlocksExtractor {

    public static List<Block> getTranslucentBlocks() {

        List<Block> translucentBlocks = new ArrayList<>();
//        for (Map.Entry<Block, RenderLayer> map : BRLMIMixin.getBlockRenderLayerMap().entrySet()) {
//            if (map.getValue() == RenderLayer.getTranslucent()) {
//                translucentBlocks.add(map.getKey());
//            }
//        }

        return translucentBlocks;
    }
}
