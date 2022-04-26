package fr.poulpogaz.animescandl.utils.math;

public class MathTestUtils {

    public static Interval i(float min, float max) {
        return new Interval(min, max);
    }

    public static Interval i(float min, boolean includeMin, float max, boolean includeMax) {
        return new Interval(min, includeMin, max, includeMax);
    }

    public static Singleton s(float value) {
        return new Singleton(value);
    }

    public static Union u(Set... sets) {
        return new Union(sets);
    }
}
