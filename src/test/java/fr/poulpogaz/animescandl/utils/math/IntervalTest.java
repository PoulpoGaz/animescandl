package fr.poulpogaz.animescandl.utils.math;

import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static fr.poulpogaz.animescandl.utils.math.MathTestUtils.intersectionTest;
import static fr.poulpogaz.animescandl.utils.math.MathTestUtils.unionTest;
import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    @Test
    void constructorTest() {
        assertDoesNotThrow(Interval::all);
        assertDoesNotThrow(() -> Interval.open(0, 10));
        assertDoesNotThrow(() -> Interval.openClosed(0, 10));
        assertDoesNotThrow(() -> Interval.closed(0, 10));
        assertDoesNotThrow(() -> Interval.closedOpen(0, 10));
        assertDoesNotThrow(() -> Interval.greaterThan(0));
        assertDoesNotThrow(() -> Interval.lessThan(0));
        assertDoesNotThrow(() -> Interval.strictLessThan(0));
        assertDoesNotThrow(() -> Interval.strictGreaterThan(0));

        assertThrows(IllegalArgumentException.class, () -> Interval.open(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Interval.openClosed(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Interval.closed(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Interval.closedOpen(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Interval.greaterThan(Float.NEGATIVE_INFINITY));
    }

    private void containsSingleTest(Interval i) {
        Bound left = i.getLeftBound();
        Bound right = i.getRightBound();

        if (left.isFinite() && right.isFinite()) {
            float mid = (right.value() + left.value()) / 2;
            float less = left.value() - 1;
            float greater = right.value() + 1;

            assertTrue(i.contains(mid));
            assertFalse(i.contains(less));
            assertFalse(i.contains(greater));

        } else if (left.isFinite() && right.isInfinity()) {
            assertTrue(i.contains(right.value() + 1));

        } else if (left.isInfinity() && right.isFinite()) {
            assertTrue(i.contains(right.value() - 1));

        } else if (left.isInfinity() && right.isInfinity()) {
            assertTrue(i.contains(0));
        }

        if (left.isOpen()) {
            assertFalse(i.contains(left.value()));
        }
        if (left.isClosed()) {
            assertTrue(i.contains(left.value()));
        }
        if (right.isOpen()) {
            assertFalse(i.contains(right.value()));
        }
        if (right.isClosed()) {
            assertTrue(i.contains(right.value()));
        }

        if (left.isInfinity()) {
            assertTrue(i.contains(Float.NEGATIVE_INFINITY));
        } else {
            assertFalse(i.contains(Float.NEGATIVE_INFINITY));
        }

        if (right.isInfinity()) {
            assertTrue(i.contains(Float.POSITIVE_INFINITY));
        } else {
            assertFalse(i.contains(Float.POSITIVE_INFINITY));
        }
    }

    @Test
    void contains() {
        containsSingleTest(Interval.open(-10, 10));
        containsSingleTest(Interval.openClosed(-10, 10));
        containsSingleTest(Interval.closedOpen(-10, 10));
        containsSingleTest(Interval.closed(-10, 10));
        containsSingleTest(Interval.greaterThan(10));
        containsSingleTest(Interval.lessThan(-10));
        containsSingleTest(Interval.strictGreaterThan(-10));
        containsSingleTest(Interval.strictLessThan(10));
        containsSingleTest(Interval.all());
    }

    void isConnectedTest(boolean expected, Interval a, Interval b, BiFunction<Interval, Interval, Boolean> test) {
        assertEquals(expected, test.apply(a, b));
        assertEquals(expected, test.apply(a, b));
        assertTrue(test.apply(a, a));
        assertTrue(test.apply(b, b));
    }

    void nonEmptyIntersection(boolean expected, Interval a, Interval b) {
        isConnectedTest(expected, a, b, Interval::nonEmptyIntersection);
    }

    void isConnected(boolean expected, Interval a, Interval b) {
        isConnectedTest(expected, a, b, Interval::isConnected);
    }

    @Test
    void connectedTest() {
        isConnected(true, Interval.all(), Interval.all());
        isConnected(true, Interval.all(), Interval.all());
        isConnected(true, Interval.greaterThan(0), Interval.open(-5, 5));
        isConnected(false, Interval.open(-5, -1), Interval.open(1, 5));

        isConnected(false, Interval.open(-5, 0), Interval.open(0, 5));
        isConnected(true, Interval.openClosed(-5, 0), Interval.open(0, 5));
        isConnected(true, Interval.open(-5, 0), Interval.closedOpen(0, 5));
        isConnected(true, Interval.openClosed(-5, 0), Interval.closedOpen(0, 5));

        isConnected(true, Interval.closed(-10, -4), Interval.openClosed(-4, -2));


        nonEmptyIntersection(true, Interval.all(), Interval.all());
        nonEmptyIntersection(true, Interval.all(), Interval.all());
        nonEmptyIntersection(true, Interval.greaterThan(0), Interval.open(-5, 5));
        nonEmptyIntersection(false, Interval.open(-5, -1), Interval.open(1, 5));

        nonEmptyIntersection(false, Interval.open(-5, 0), Interval.open(0, 5));
        nonEmptyIntersection(false, Interval.openClosed(-5, 0), Interval.open(0, 5));
        nonEmptyIntersection(false, Interval.open(-5, 0), Interval.closedOpen(0, 5));
        nonEmptyIntersection(true, Interval.openClosed(-5, 0), Interval.closedOpen(0, 5));

        nonEmptyIntersection(false, Interval.closed(-10, -4), Interval.openClosed(-4, -2));
    }

    @Test
    void union() {
        unionTest(Interval.open(-10, 0),
                Interval.open(0, 10),
                new Union(Interval.open(-10, 0), Interval.open(0, 10)));

        unionTest(Interval.closed(-10, 0), Interval.openClosed(-10, 10), Interval.closed(-10, 10));
        unionTest(Interval.closed(0, 10), Interval.closedOpen(-10, 10), Interval.closed(-10, 10));

        Interval i = Interval.open(-10, 10);
        unionTest(Interval.open(-10, 0), Interval.closedOpen(0, 10), i);
        unionTest(Interval.openClosed(-10, 0), Interval.open(0, 10), i);
        unionTest(Interval.openClosed(-10, 0), Interval.closedOpen(0, 10), i);

        unionTest(Interval.all(), Interval.greaterThan(10), Interval.all());
        unionTest(Interval.lessThan(0), Interval.greaterThan(0), Interval.all());
        unionTest(Interval.strictLessThan(0),
                Interval.strictGreaterThan(0),
                new Union(Interval.strictLessThan(0), Interval.strictGreaterThan(0)));

        unionTest(Interval.closed(10, 15), Interval.closed(0, 5),
                new Union(Interval.closed(10, 15), Interval.closed(0, 5)));
    }

    @Test
    void intersect() {
        intersectionTest(Interval.open(-10, 10), Interval.open(0, 10), Interval.open(0, 10));
        intersectionTest(Interval.open(-10, 10), Interval.closed(0, 10), Interval.closedOpen(0, 10));
        intersectionTest(Interval.open(-10, 10), Interval.closed(-10, 0), Interval.openClosed(-10, 0));
        intersectionTest(Interval.closed(-10, -4), Interval.openClosed(-4, -2), Empty.INSTANCE);
        intersectionTest(Interval.lessThan(0), Interval.greaterThan(0), new Singleton(0));
    }
}