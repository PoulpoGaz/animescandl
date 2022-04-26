package fr.poulpogaz.animescandl.utils.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An union DOESN'T CONTAIN an union
 */
public class Union implements Set {

    private final List<Set> sets;

    public Union(List<Set> sets) {
        this.sets = sets;

        for (Set s : sets) {
            if (s instanceof Union) {
                throw new IllegalStateException("An union can't contain an union");
            }
        }
    }

    public Union(Set... sets) {
        this(List.of(sets));
    }

    @Override
    public boolean contains(float real) {
        for (Set s : sets) {
            if (s.contains(real)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set union(Set set) {
        if (set == this) {
            return this;
        }

        if (set instanceof Union u) {

        } else {

        }

        return null;
    }

    @Override
    public Set intersect(Set set) {
        if (set == this) {
            return this;
        }

        // union vs union
        if (set instanceof Union u) {
            List<Set> others = u.getSets();

            if (others.size() == 0) {
                return Empty.INSTANCE;
            } else if (others.size() == 1) {
                return intersect(others.get(0));
            } else {

                List<Set> sets = new ArrayList<>();

                for (Set s : this.sets) {
                    // became union vs (not an union)
                    Set i = s.intersect(set);

                    if (i != Empty.INSTANCE) {
                        sets.add(i);
                    }
                }

                if (sets.size() == 0) {
                    return Empty.INSTANCE;
                } else if (sets.size() == 1) {
                    return sets.get(0);
                } else {
                    return new Union(Collections.unmodifiableList(sets));
                }
            }

        } else {
            // union vs singleton or union vs interval
            Set out = set;

            for (Set set2 : sets) {
                out = out.intersect(set2);

                if (out instanceof Empty) {
                    break;
                }
            }

            return out;
        }
    }

    public List<Set> getSets() {
        return sets;
    }
}
