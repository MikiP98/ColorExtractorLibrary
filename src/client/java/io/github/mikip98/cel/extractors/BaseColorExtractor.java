package io.github.mikip98.cel.extractors;

import io.github.mikip98.cel.structures.ColorRGBA;
import io.github.mikip98.cel.structures.ColorReturn;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public abstract class BaseColorExtractor {

    public static ColorRGBA postProcessData(ColorReturn colorReturn, float weightedness) {
        if (weightedness > 1 || weightedness < 0) {
            LOGGER.error("weightedness is not between 0 and 1: {}", weightedness);
            throw new RuntimeException("weightedness is not between 0 and 1");
        }

        if (Math.round(colorReturn.color_avg.r) > 255 || Math.round(colorReturn.color_avg.g) > 255 || Math.round(colorReturn.color_avg.b) > 255 || Math.round(colorReturn.color_avg.a) > 255) {
            LOGGER.error("Color values are out of range: {}; {}; {}; {}", colorReturn.color_avg.r, colorReturn.color_avg.g, colorReturn.color_avg.b, colorReturn.color_avg.a);
            throw new RuntimeException("Color values are out of range");
        }

        double weight_sum = colorReturn.weight_sum;

        if (weight_sum == 0) {
            LOGGER.error("weight_sum is 0");
            return new ColorRGBA(0, 0, 0, 0);
        }

        ColorRGBA color_weighted_avg = colorReturn.color_avg;
        ColorRGBA color_sum = colorReturn.color_sum;

//        LOGGER.info("color_sum.r: {}; color_sum.g: {}; color_sum.b: {}; color_sum.a: {}; weight_sum: {}", color_sum.r, color_sum.g, color_sum.b, color_sum.a, weight_sum);
//        LOGGER.info("color_sum.r / weight_sum = {} / {} = {}", color_sum.r, weight_sum, color_sum.r / weight_sum);
//        LOGGER.info("color_sum.g / weight_sum = {} / {} = {}", color_sum.g, weight_sum, color_sum.g / weight_sum);
//        LOGGER.info("color_sum.b / weight_sum = {} / {} = {}", color_sum.b, weight_sum, color_sum.b / weight_sum);
//        LOGGER.info("color_sum.a / weight_sum = {} / {} = {}", color_sum.a, weight_sum, color_sum.a / weight_sum);

        ColorRGBA color_avg = new ColorRGBA(
                color_sum.r / weight_sum,
                color_sum.g / weight_sum,
                color_sum.b / weight_sum,
                color_sum.a / weight_sum
        );

        if (Math.round(color_avg.r) > 255 || Math.round(color_avg.g) > 255 || Math.round(color_avg.b) > 255 || Math.round(color_avg.a) > 255) {
            LOGGER.error("Color values are out of range: {}; {}; {}; {}", color_avg.r, color_avg.g, color_avg.b, color_avg.a);
            throw new RuntimeException("Color values are out of range");
        }

        return new ColorRGBA(
                color_weighted_avg.r * weightedness + color_avg.r * (1 - weightedness),
                color_weighted_avg.g * weightedness + color_avg.g * (1 - weightedness),
                color_weighted_avg.b * weightedness + color_avg.b * (1 - weightedness),
                color_weighted_avg.a * weightedness + color_avg.a * (1 - weightedness)
        );
    }
}
