package io.github.mikip98.cel.api;

import io.github.mikip98.cel.enums.AVGTypes;

// This is the only class that mods using this lib should use/the only one that guarantees stability
public class ColorExtractorAPI {
    // ------------------------------------------------------------------------
    // ----------------------- COLOR AVERAGE EXTRACTORS -----------------------
    // ------------------------------------------------------------------------

    public static int[] getAverageColorForBlockstate(String ModID, String BlockstateID, float weightedness) {
        return getAverageColorForBlockstate(ModID, BlockstateID, weightedness, AVGTypes.WEIGHTED_ARITHMETIC);
    }
    public static int[] getAverageColorForBlockstate(String ModID, String BlockstateID, float weightedness, AVGTypes AVGType) {
        return new int[0];
    }

    public static int[] getAverageColorForBlockModel(String ModID, String ModelID, float weightedness) {
        return new int[0];
    }

    public static int[] getAverageColorForBlockTexture(String ModID, String TextureID, AVGTypes AVGType) {
        return new int[0];
    }



    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
