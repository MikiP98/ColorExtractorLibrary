package io.github.mikip98.cel.extractors;

import io.github.mikip98.cel.structures.ColorRGBA;
import io.github.mikip98.cel.structures.ColorReturn;

public class BaseColorExtractor {
    public static ColorRGBA postProcessData(ColorReturn colorReturn, float weightedness) {
        ColorRGBA color_weighted_avg = colorReturn.color_avg;

        ColorRGBA color_sum = colorReturn.color_sum;
        double weight_sum = colorReturn.weight_sum;
        ColorRGBA color_avg = new ColorRGBA(
                color_sum.r / weight_sum,
                color_sum.g / weight_sum,
                color_sum.b / weight_sum,
                color_sum.a / weight_sum
        );

        return new ColorRGBA(
                color_weighted_avg.r * weightedness + color_avg.r * (1 - weightedness),
                color_weighted_avg.g * weightedness + color_avg.g * (1 - weightedness),
                color_weighted_avg.b * weightedness + color_avg.b * (1 - weightedness),
                color_weighted_avg.a * weightedness + color_avg.a * (1 - weightedness)
        );
    }
}
