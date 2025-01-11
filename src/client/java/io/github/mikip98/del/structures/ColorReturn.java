package io.github.mikip98.del.structures;

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
        this.color_avg = new ColorRGBA();
        this.color_sum = new ColorRGBA();
        this.weight_sum = 0;
    }

    public ColorReturn add(ColorReturn other) {
        this.color_avg.add(other.color_avg);
        this.color_sum.add(other.color_sum);
        this.weight_sum += other.weight_sum;
        return this;
    }


    @Override
    public String toString() {
        return "ColorReturn{" +
                "color_avg=" + color_avg +
                ", color_sum=" + color_sum +
                ", weight_sum=" + weight_sum +
                '}';
    }
}
