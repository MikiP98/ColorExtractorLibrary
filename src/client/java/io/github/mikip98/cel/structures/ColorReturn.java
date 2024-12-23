package io.github.mikip98.cel.structures;

public class ColorReturn {
    public ColorRGBA color_avg;
    public ColorRGBA color_sum;
    public double weight_sum;

    public ColorReturn(ColorRGBA color_avg, ColorRGBA color_sum, double weight_sum) {
        this.color_avg = color_avg;
        this.color_sum = color_sum;
        this.weight_sum = weight_sum;
    }

    public ColorReturn() {
    }
}
