package io.github.mikip98.del.extractors;

import io.github.mikip98.del.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


public class TranslucentBlocksExtractor {

    public static @NotNull Map<String, List<String>> getTranslucentBlocks() {
        return Util.renderLayerModBlockMap.get("translucent");
    }
}
