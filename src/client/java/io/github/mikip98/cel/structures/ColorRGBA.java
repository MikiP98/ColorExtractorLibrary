package io.github.mikip98.cel.structures;

import java.util.logging.Logger;

import static java.lang.Double.NaN;

import static io.github.mikip98.cel.ColorExtractorLibraryClient.LOGGER;

public class ColorRGBA {
    public double r;
    public double g;
    public double b;
    public double a;

    public ColorRGBA(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorRGBA() {
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.a = 0;
    }

    public void add(ColorRGBA other) {
        this.r += other.r;
        this.g += other.g;
        this.b += other.b;
        this.a += other.a;

        if (Double.isNaN(this.r) || Double.isNaN(this.g) || Double.isNaN(this.b) || Double.isNaN(this.a) || Double.isNaN(other.r) || Double.isNaN(other.g) || Double.isNaN(other.b) || Double.isNaN(other.a)) {
            LOGGER.error("NaN; r: {}; g: {}; b: {}; a: {}; other.r: {}; other.g: {}; other.b: {}; other.a: {}", this.r, this.g, this.b, this.a, other.r, other.g, other.b, other.a);
            throw new RuntimeException("NaN");
        }
    }
    public void divide(int n) {
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;

        if (Double.isNaN(this.r) || Double.isNaN(this.g) || Double.isNaN(this.b) || Double.isNaN(this.a)) {
            LOGGER.error("NaN; r: {}; g: {}; b: {}; a: {}; n: {}", this.r, this.g, this.b, this.a, n);
            throw new RuntimeException("NaN");
        }
    }

    @Override
    public String toString() {
        return "ColorRGBA{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}
