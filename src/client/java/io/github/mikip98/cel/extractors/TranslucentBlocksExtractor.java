package io.github.mikip98.cel.extractors;

import io.github.mikip98.cel.util.Util;

import java.util.List;
import java.util.Map;


public class TranslucentBlocksExtractor {

    public static Map<String, List<String>> getTranslucentBlocks() {
        return Util.renderLayerModBlockMap.get("translucent");
    }
}
