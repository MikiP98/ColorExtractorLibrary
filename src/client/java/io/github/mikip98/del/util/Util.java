package io.github.mikip98.del.util;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

import java.util.List;
import java.util.Map;

public class Util {
    public static Map<Block, RenderLayer> rawBlockRenderLayerMap;
    public static Map<String, Map<String, List<String>>> renderLayerModBlockMap;

    public static final int colorCacheTimeoutMinutes = 30;
}
