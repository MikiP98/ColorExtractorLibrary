package io.github.mikip98.del.structures;

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


    public ColorRGBA copy() {
        return new ColorRGBA(this.r, this.g, this.b, this.a);
    }

    public double getMaxRGB() {
        return Math.max(r, Math.max(g, b));
    }


    public ColorRGBA add(ColorRGBA other) {
        this.r += other.r;
        this.g += other.g;
        this.b += other.b;
        this.a += other.a;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ColorRGBA multiply(ColorRGBA other) {
        this.r *= other.r;
        this.g *= other.g;
        this.b *= other.b;
        this.a *= other.a;
        return this;
    }
    public ColorRGBA multiply(double n) {
        this.r *= n;
        this.g *= n;
        this.b *= n;
        this.a *= n;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ColorRGBA divide(double n) {
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;
        return this;
    }
    @SuppressWarnings("UnusedReturnValue")
    public ColorRGBA divide(int n) {
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ColorRGBA root(double n) {
        this.r = Math.pow(this.r, 1 / n);
        this.g = Math.pow(this.g, 1 / n);
        this.b = Math.pow(this.b, 1 / n);
        this.a = Math.pow(this.a, 1 / n);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ColorRGBA round() {
        this.r = Math.round(this.r);
        this.g = Math.round(this.g);
        this.b = Math.round(this.b);
        this.a = Math.round(this.a);
        return this;
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
