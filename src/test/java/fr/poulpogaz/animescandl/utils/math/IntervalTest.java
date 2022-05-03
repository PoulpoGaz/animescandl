package fr.poulpogaz.animescandl.utils.math;

import org.junit.jupiter.api.Test;

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

    private void constainsSingleTest(Interval i) {
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
        constainsSingleTest(Interval.open(-10, 10));
        constainsSingleTest(Interval.openClosed(-10, 10));
        constainsSingleTest(Interval.closedOpen(-10, 10));
        constainsSingleTest(Interval.closed(-10, 10));
        constainsSingleTest(Interval.greaterThan(10));
        constainsSingleTest(Interval.lessThan(-10));
        constainsSingleTest(Interval.strictGreaterThan(-10));
        constainsSingleTest(Interval.strictLessThan(10));
        constainsSingleTest(Interval.all());
    }

    void isConnectedTest(boolean expected, Interval a, Interval b) {
        assertEquals(expected, a.isConnected(b));
        assertEquals(expected, b.isConnected(a));
        assertTrue(a.isConnected(a));
        assertTrue(b.isConnected(b));
    }

    @Test
    void isConnected() {
        isConnectedTest(true, Interval.all(), Interval.all());
        isConnectedTest(true, Interval.all(), Interval.all());
        isConnectedTest(true, Interval.greaterThan(0), Interval.open(-5, 5));
        isConnectedTest(false, Interval.open(-5, -1), Interval.open(1, 5));

        isConnectedTest(false, Interval.open(-5, 0), Interval.open(0, 5));
        isConnectedTest(true, Interval.openClosed(-5, 0), Interval.open(0, 5));
        isConnectedTest(true, Interval.open(-5, 0), Interval.closedOpen(0, 5));
        isConnectedTest(true, Interval.openClosed(-5, 0), Interval.closedOpen(0, 5));
    }

    void unionTest(Interval a, Interval b, Set expected) {
        assertEquals(expected, a.union(b));
        assertEquals(expected, b.union(a));
        assertEquals(a, a.union(a));
        assertEquals(b, b.union(b));
    }

    @Test
    void union() {
        // Currently, doesn't work. But, it's because Union aren't finished
        // and output seems correct
        //unionTest(Interval2.open(-10, 0),
        //        Interval2.open(0, 10),
        //        new Union(Interval2.open(-10, 0), Interval2.open(0, 10)));

        unionTest(Interval.closed(-10, 0), Interval.openClosed(-10, 10), Interval.closed(-10, 10));
        unionTest(Interval.closed(0, 10), Interval.closedOpen(-10, 10), Interval.closed(-10, 10));

        Interval i = Interval.open(-10, 10);
        unionTest(Interval.open(-10, 0), Interval.closedOpen(0, 10), i);
        unionTest(Interval.openClosed(-10, 0), Interval.open(0, 10), i);
        unionTest(Interval.openClosed(-10, 0), Interval.closedOpen(0, 10), i);

        unionTest(Interval.all(), Interval.greaterThan(10), Interval.all());
        unionTest(Interval.lessThan(0), Interval.greaterThan(0), Interval.all());
        //unionTest(Interval2.strictLessThan(0),
        //        Interval2.strictGreaterThan(0),
        //        new Union(Interval2.strictLessThan(0), Interval2.greaterThan(0)));

        //unionTest(Interval2.closed(10, 15), Interval2.closed(0, 5),
        //        new Union(Interval2.closed(10, 15), Interval2.closed(0, 5)));
    }

    void intersectionTest(Interval a, Interval b, Set expected) {
        assertEquals(expected, a.intersect(b));
        assertEquals(expected, b.intersect(a));
        assertEquals(a, a.intersect(a));
        assertEquals(b, b.intersect(b));
    }

    @Test
    void intersect() {
        intersectionTest(Interval.open(-10, 10), Interval.open(0, 10), Interval.open(0, 10));
        intersectionTest(Interval.open(-10, 10), Interval.closed(0, 10), Interval.closedOpen(0, 10));
        intersectionTest(Interval.open(-10, 10), Interval.closed(-10, 0), Interval.openClosed(-10, 0));
    }
}