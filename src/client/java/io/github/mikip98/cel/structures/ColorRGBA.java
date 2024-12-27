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


    public ColorRGBA copy() {
        return new ColorRGBA(this.r, this.g, this.b, this.a);
    }

    public double getMaxRGBA() {
        return Math.max(r, Math.max(g, Math.max(b, a)));
    }
    public double getMaxRGB() {
        return Math.max(r, Math.max(g, b));
    }


    public void add(ColorRGBA other) {
        this.r += other.r;
        this.g += other.g;
        this.b += other.b;
        this.a += other.a;
    }

    public void multiply(ColorRGBA other) {
        this.r *= other.r;
        this.g *= other.g;
        this.b *= other.b;
        this.a *= other.a;
    }
    public void multiply(double n) {
        this.r *= n;
        this.g *= n;
        this.b *= n;
        this.a *= n;
    }

    public void divide(double n) {
//        if (n == 0) throw new RuntimeException("Division by zero");
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;
    }
    public void divide(int n) {
//        if (n == 0) throw new RuntimeException("Division by zero");
        this.r /= n;
        this.g /= n;
        this.b /= n;
        this.a /= n;
    }

    public void root(double n) {
        this.r = Math.pow(this.r, 1 / n);
        this.g = Math.pow(this.g, 1 / n);
        this.b = Math.pow(this.b, 1 / n);
        this.a = Math.pow(this.a, 1 / n);
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
