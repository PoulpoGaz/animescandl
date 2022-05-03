package fr.poulpogaz.animescandl.utils.math;

import java.util.List;

public record Singleton(float value) implements Set {

    @Override
    public boolean contains(float real) {
        return value == real;
    }

    @Override
    public Set union(Set set) {
        if (set.contains(value)) {
            return set;
        } else {
            return new Union(List.of(set, this));
        }
    }

    @Override
    public Set intersect(Set set) {
        if (set.contains(value)) {
            return this;
        } else {
            return null;
        }
    }

    //@Override
    //public float sup() {
    //    return value;
    //}

    //@Override
    //public float inf() {
    //    return value;
    //}
}