package fr.poulpogaz.animescandl.utils.math;

import java.util.Objects;

/**
 *                     FRENCH NOTATION        ENGLISH NOTATION
 *
 * PositiveInfinity = +inf[                   +inf)
 * NegativeInfinity = ]-inf                   (-inf
 * OpenBracket      = [val  / val[            [val / val)
 * ClosingBracket    = ]val  / val]            (val / val]
 */
public interface Bound {

    Bound POSITIVE_INFINITY = new PositiveInfinity();
    Bound NEGATIVE_INFINITY = new NegativeInfinity();

    boolean lessThan(float value);

    boolean greaterThan(float value);

    boolean isFinite();

    default boolean isInfinity() {
        return isPositiveInfinite() || isNegativeInfinite();
    }

    boolean isOpen();

    default boolean isClosed() {
        return !isOpen();
    }

    boolean isPositiveInfinite();

    boolean isNegativeInfinite();

    float value();


    static int valueCompare(Bound a, Bound b) {
        return Float.compare(a.value(), b.value());
    }

    static int minValueCompare(Bound a, Bound b) {
        int c = Float.compare(a.value(), b.value());

        if (c == 0 && a.isFinite()) {
            if (a.isOpen() && b.isClosed()) {
                return 1;
            } else if (a.isClosed() && b.isOpen()) {
                return -1;
            } else {
                return 0;
            }
        }

        return c;
    }

    static int maxValueCompare(Bound a, Bound b) {
        int c = Float.compare(b.value(), a.value());

        if (c == 0 && a.isFinite()) {
            if (a.isOpen() && b.isClosed()) {
                return -1;
            } else if (a.isClosed() && b.isOpen()) {
                return 1;
            } else {
                return 0;
            }
        }

        return c;
    }


    abstract class Finite implements Bound {

        protected final float value;

        private Finite(float value) {
            if (Float.isInfinite(value) || Float.isNaN(value)) {
                throw new IllegalArgumentException(value + " is infinite or NaN");
            }

            this.value = value;
        }

        @Override
        public boolean isFinite() {
            return true;
        }

        @Override
        public boolean isPositiveInfinite() {
            return false;
        }

        @Override
        public boolean isNegativeInfinite() {
            return false;
        }

        @Override
        public float value() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Finite f) {
                return value == f.value && isOpen() == f.isOpen();
            }

            return false;
        }
    }

    class OpenFinite extends Finite {

        public OpenFinite(float value) {
            super(value);
        }

        @Override
        public boolean lessThan(float value) {
            return this.value < value;
        }

        @Override
        public boolean greaterThan(float value) {
            return this.value > value;
        }

        @Override
        public boolean isOpen() {
            return true;
        }
    }

    class ClosedFinite extends Finite {

        public ClosedFinite(float value) {
            super(value);
        }

        @Override
        public boolean lessThan(float value) {
            return this.value <= value;
        }

        @Override
        public boolean greaterThan(float value) {
            return this.value >= value;
        }

        @Override
        public boolean isOpen() {
            return false;
        }
    }

    final class PositiveInfinity implements Bound {

        @Override
        public boolean lessThan(float value) {
            return false;
        }

        @Override
        public boolean greaterThan(float value) {
            return true;
        }

        @Override
        public boolean isFinite() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isPositiveInfinite() {
            return true;
        }

        @Override
        public boolean isNegativeInfinite() {
            return false;
        }

        @Override
        public float value() {
            return Float.POSITIVE_INFINITY;
        }

        // Only one instance of PositiveInfinity
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    }

    final class NegativeInfinity implements Bound {

        @Override
        public boolean lessThan(float value) {
            return true;
        }

        @Override
        public boolean greaterThan(float value) {
            return false;
        }

        @Override
        public boolean isFinite() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isPositiveInfinite() {
            return false;
        }

        @Override
        public boolean isNegativeInfinite() {
            return true;
        }

        @Override
        public float value() {
            return Float.NEGATIVE_INFINITY;
        }

        // Only one instance of NegativeInfinity
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    }
}