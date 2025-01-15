package io.github.mikip98.del.api;

import io.github.mikip98.del.enums.AVGTypes;
import io.github.mikip98.del.extractors.color.BlockModelColorExtractor;
import io.github.mikip98.del.extractors.color.BlockstateColorExtractor;
import io.github.mikip98.del.extractors.color.TextureColorExtractor;
import io.github.mikip98.del.structures.ColorReturn;

import java.util.ArrayList;

// This is the one of the classed that mods using this lib should use/one of the ones that guarantee stability
@SuppressWarnings("unused")
public class ColorExtractionAPI {

    // ------------------------------------------------------------------------
    // ----------------------- AVERAGE COLOR EXTRACTORS -----------------------
    // ------------------------------------------------------------------------

    /**
     * Returns the average color for a blockstate.
     *
     * @param modId the ID of the mod that owns the blockstate
     * @param blockstateId the ID of the blockstate
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modId, String blockstateId) {
        return getAverageColorForBlockstate(modId, blockstateId, 0.8f, AVGTypes.WEIGHTED_ARITHMETIC);
    }
    /**
     * Returns the average color for a blockstate.
     *
     * @param modId the ID of the mod that owns the blockstate
     * @param blockstateId the ID of the blockstate
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modId, String blockstateId, AVGTypes avgType) {
        return getAverageColorForBlockstate(modId, blockstateId, 0.8f, avgType);
    }
    /**
     * Returns the average color for a blockstate.
     *
     * @param modId the ID of the mod that owns the blockstate
     * @param blockstateId the ID of the blockstate
     * @param weightedness the weightedness of the color extraction; should be between 0 and 1
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modId, String blockstateId, float weightedness) {
        return getAverageColorForBlockstate(modId, blockstateId, weightedness, AVGTypes.WEIGHTED_ARITHMETIC);
    }
    /**
     * Returns the average color for a blockstate using the specified average type.
     *
     * @param modId the ID of the mod that owns the blockstate
     * @param blockstateId the ID/name of the blockstate
     * @param weightedness the weightedness of the color extraction; should be between 0 and 1
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modId, String blockstateId, float weightedness, AVGTypes avgType) {
        weightedness = validateWeightedness(weightedness);
        return BlockstateColorExtractor.getAverageBlockstateColor(modId, blockstateId, new ArrayList<>(), weightedness, avgType);
    }


    /**
     * Returns the average color for a block model.
     *
     * @param modId the ID of the mod that owns the block model
     * @param modelId the ID/name of the block model
     * @param weightedness the weightedness of the color extraction, should be between 0 and 1
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockModel(String modId, String modelId, float weightedness, AVGTypes avgType) {
        weightedness = validateWeightedness(weightedness);
        return BlockModelColorExtractor.getAverageModelColor(modId, modelId, weightedness, avgType);
    }


    /**
     * Returns the average color for a texture.
     *
     * @param modId the ID of the mod that owns the texture
     * @param textureId the ID/name of the texture
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockTexture(String modId, String textureId, float weightedness, AVGTypes avgType) {
        weightedness = validateWeightedness(weightedness);
        return TextureColorExtractor.getAverageTextureColor(modId, textureId, weightedness, avgType);
    }

    /**
     * Validates the weightedness value to ensure it is between 0 and 1.
     *
     * @param weightedness the weightedness value to validate
     * @return the validated weightedness value clamped between 0 and 1
     */
    protected static float validateWeightedness(float weightedness) {
        return Math.max(Math.min(weightedness, 1), 0);  // REMEMBER: Switch to Math.clamp() on MC 1.21+ (JAVA 21+)
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
