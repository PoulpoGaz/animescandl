package fr.poulpogaz.animescandl.utils.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A union doesn't contain a union.
 * It contains an ordered list of set sorted by the return of {@link Set#inf()}.
 * All sets should not overlap.
 *
 *
 */
public class Union implements Set {

    private final List<Set> sets;

    public Union(List<Set> sets) {
        List<Set> copy;
        if (sets instanceof ArrayList<Set>) {
            copy = sets;
        } else {
            copy = new ArrayList<>(sets);
        }

        checkValid(copy);
        this.sets = copy.stream()
                .sorted(Comparator.comparing(Set::inf))
                .toList();
    }

    public Union(Set... sets) {
        this(List.of(sets));
    }

    private void checkValid(List<Set> sets) {
        for (int i = 0; i < sets.size(); i++) {
            Set s = sets.get(i);

            if (s instanceof Union u) {
                sets.addAll(i, u.getSets());
                sets.remove(u);
                i--;

            } else if (s instanceof Empty) {
                sets.remove(i);
                i--;

            } else {

                // check if s has an empty intersection with the previous sets.
                // The reverse loop avoid to check the instance of the set.
                for (int j = i - 1; j >= 0; j--) {
                    Set s2 = sets.get(j);

                    if (s.intersect(s2) != Empty.INSTANCE) {
                        System.out.println(s + " inter " + s2 + " = " + s.intersect(s2));
                        throw new IllegalArgumentException("Non empty intersection");
                    }
                }
            }
        }

        if (sets.size() < 2) {
            throw new IllegalArgumentException("A union contains at least 2 elements");
        }
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

    /**
     * @return The first index i where sets.(i).sup() >= inf
     */
    private int firstOverlapIndex(float inf) {
        for (int i = 0; i < sets.size(); i++) {
            Set s = sets.get(i);

            if (s.sup() >= inf) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return The last index i where sets.(i).inf() <= sup
     */
    private int lastOverlapIndex(float sup) {
        for (int i = sets.size() - 1; i >= 0; i--) {
            Set s = sets.get(i);

            if (s.inf() <= inf()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Set intersect(Set set) {
        if (set == this) {
            return this;
        }

        if (inf() > set.sup() || set.inf() >= sup()) {
            return Empty.INSTANCE;
        }

        int start = firstOverlapIndex(set.inf());
        int end = lastOverlapIndex(set.sup());

        System.out.println(start + " - " + end);

       return null;
    }

    @Override
    public float sup() {
        return sets.get(sets.size() - 1).sup();
    }

    @Override
    public float inf() {
        return sets.get(0).inf();
    }

    public List<Set> getSets() {
        return sets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Union union = (Union) o;

        return sets.equals(union.sets);
    }

    @Override
    public int hashCode() {
        return sets.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sets.size() - 1; i++) {
            sb.append(sets.get(i));
            sb.append(" U ");
        }

        sb.append(sets.get(sets.size() - 1));

        return sb.toString();
    }
}
