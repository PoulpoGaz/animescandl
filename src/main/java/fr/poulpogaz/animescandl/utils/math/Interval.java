package fr.poulpogaz.animescandl.utils.math;

public record Interval(float min, boolean includeMin, float max, boolean includeMax) implements Set {

    public Interval(float min, boolean includeMin, float max, boolean includeMax) {
        this.min = min;
        this.max = max;
        this.includeMin = includeMin;
        this.includeMax = includeMax;

        if (min > max) {
            throw new IllegalStateException("min > max: " + min + " > " + max);
        }
    }

    public Interval(float min, float max) {
        this(min, true, max, true);
    }

    @Override
    public boolean contains(float real) {
        if (min < real && real < max) {
            return true;
        }
        if (includeMin && min == real) {
            return true;
        }

        return includeMax && max == real;
    }

    @Override
    public Set union(Set set) {
        if (set == this) {
            return this;
        }

        if (set instanceof Interval i) {
            float min2 = Math.min(min, i.min());
            float max2 = Math.max(max, i.max());

            boolean includeMin = min2 == min ? this.includeMin : i.includeMin;
            boolean includeMax = max2 == max ? this.includeMax : i.includeMax;

            if (i.max() < min) {
                return new Union(this, i);
            } else {
                return new Interval(min2, includeMin, max2, includeMax);
            }
        } else {
            return set.union(this);
        }
    }

    @Override
    public Set intersect(Set set) {
        if (set == this) {
            return this;
        }

        if (set instanceof Interval i) {
            float min2 = Math.max(min, i.min());
            float max2 = Math.min(max, i.max());

            if (min2 > max2) {
                return Empty.INSTANCE;
            } else {
                boolean includeMin = min2 == min ? this.includeMin : i.includeMin;
                boolean includeMax = max2 == max ? this.includeMax : i.includeMax;

                return new Interval(min2, includeMin, max2, includeMax);
            }
        } else {
            return set.intersect(this);
        }
    }
}
