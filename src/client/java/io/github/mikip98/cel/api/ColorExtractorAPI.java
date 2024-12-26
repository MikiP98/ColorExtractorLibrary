package io.github.mikip98.cel.api;

import io.github.mikip98.cel.enums.AVGTypes;
import io.github.mikip98.cel.assetloading.AssetPathResolver;
import io.github.mikip98.cel.extractors.BlockstateColorExtractor;
import io.github.mikip98.cel.structures.ColorReturn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// This is the only class that mods using this lib should use/the only one that guarantees stability
@SuppressWarnings("unused")
public class ColorExtractorAPI {
    // ------------------------------------------------------------------------
    // --------------------------------- SETUP --------------------------------
    // ------------------------------------------------------------------------

    public static void cachePathsIfNotCached() {
        AssetPathResolver.cachePathsIfNotCached();
    }

    public static boolean updatePathCache() { return AssetPathResolver.updatePathCache(); }

    public static boolean clearPathCache() { return AssetPathResolver.clearPathCache(); }

    // Prevents the clearing of the path cache
    public static boolean lockPathCache() { return lockPathCache(false); }

    public static boolean lockPathCache(short code) { return lockPathCache(false, code); }
    public static boolean lockPathCache(int time) { return lockPathCache(false, time); }
    public static boolean lockPathCache(boolean isCommandProof) { return lockPathCache(isCommandProof, 1800); }

    public static boolean lockPathCache(boolean isCommandProof, short code) { return lockPathCache(isCommandProof, 1800, code); }
    public static boolean lockPathCache(boolean isCommandProof, int time) { return lockPathCache(isCommandProof, time, (short) -1); }

    public static boolean lockPathCache(boolean isCommandProof, int time, short code) {
        AssetPathResolver.addPathsLock(code);
        return false;
    }

    public static boolean unlockPathCache() { return unlockPathCache((short) -1); }
    public static boolean unlockPathCache(short code) {
        AssetPathResolver.removePathsLock(code);
        return false;
    }



    // ------------------------------------------------------------------------
    // ----------------------- COLOR AVERAGE EXTRACTORS -----------------------
    // ------------------------------------------------------------------------

    /**
     * Returns the average color for a blockstate.
     *
     * @param modID the ID of the mod that owns the blockstate
     * @param blockstateID the ID of the blockstate
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modID, String blockstateID) {
        return getAverageColorForBlockstate(modID, blockstateID, 1.0f, AVGTypes.WEIGHTED_ARITHMETIC);
    }
    /**
     * Returns the average color for a blockstate.
     *
     * @param modID the ID of the mod that owns the blockstate
     * @param blockstateID the ID of the blockstate
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modID, String blockstateID, AVGTypes avgType) {
        return getAverageColorForBlockstate(modID, blockstateID, 1.0f, avgType);
    }
    /**
     * Returns the average color for a blockstate.
     *
     * @param modID the ID of the mod that owns the blockstate
     * @param blockstateID the ID of the blockstate
     * @param weightedness the weightedness of the color extraction; should be between 0 and 1
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modID, String blockstateID, float weightedness) {
        return getAverageColorForBlockstate(modID, blockstateID, weightedness, AVGTypes.WEIGHTED_ARITHMETIC);
    }
    /**
     * Returns the average color for a blockstate using the specified average type.
     *
     * @param modID the ID of the mod that owns the blockstate
     * @param blockstateID the ID/name of the blockstate
     * @param weightedness the weightedness of the color extraction; should be between 0 and 1
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static ColorReturn getAverageColorForBlockstate(String modID, String blockstateID, float weightedness, AVGTypes avgType) {
        weightedness = validateWeightedness(weightedness);
        return BlockstateColorExtractor.getAverageBlockstateColor(modID, blockstateID, new ArrayList<>(), weightedness, avgType);
    }


    /**
     * Returns the average color for a block model.
     *
     * @param modID the ID of the mod that owns the block model
     * @param modelID the ID/name of the block model
     * @param weightedness the weightedness of the color extraction, should be between 0 and 1
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static int @NotNull [] getAverageColorForBlockModel(String modID, String modelID, float weightedness, AVGTypes avgType) {
        weightedness = validateWeightedness(weightedness);
        return new int[0];
    }


    /**
     * Returns the average color for a texture.
     *
     * @param modID the ID of the mod that owns the texture
     * @param textureID the ID/name of the texture
     * @param avgType the type of average to use for the color extraction
     * @return the average color as an array of RGB values
     */
    public static int @NotNull [] getAverageColorForBlockTexture(String modID, String textureID, AVGTypes avgType) {
        return new int[0];
    }

    /**
     * Validates the weightedness value to ensure it is between 0 and 1.
     *
     * @param weightedness the weightedness value to validate
     * @return the validated weightedness value clamped between 0 and 1
     */
    public static float validateWeightedness(float weightedness) {
        return Math.max(Math.min(weightedness, 1), 0);  // REMEMBER: Switch to Math.clamp() on MC 1.21+ (JAVA 21+)
    }



    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
}
