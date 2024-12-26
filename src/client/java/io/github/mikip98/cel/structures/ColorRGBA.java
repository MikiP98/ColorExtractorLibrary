package io.github.mikip98.cel.structures;

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
    }
    public void divide(int n) {
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;
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
