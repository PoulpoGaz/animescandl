package fr.poulpogaz.animescandl.utils.math;

import org.junit.jupiter.api.Test;

import static fr.poulpogaz.animescandl.utils.math.MathTestUtils.i;
import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    @Test
    void contains() {
        assertTrue(i(-5, 5).contains(-5));
        assertTrue(i(-5, 5).contains(5));
        assertFalse(i(-5, 5).contains(10));
        assertFalse(i(-5, 5).contains(-10));

        assertTrue(i(-5, true, 5, false).contains(-5));
        assertFalse(i(-5, true, 5, false).contains(5));

        assertFalse(i(-5, false, 5, true).contains(-5));
        assertTrue(i(-5, false, 5, true).contains(5));

        assertFalse(i(-5, false, 5, false).contains(-5));
        assertFalse(i(-5, false, 5, false).contains(5));
    }

    @Test
    void union() {
        // [0;5]U[5;10]=[0;10]
        assertEquals(i(0, 5).union(i(5, 10)), i(0, 10));

        // ]0;5]U[5;10]=]0;10]
        assertEquals(i(0, false, 5, true).union(i(5, 10)), i(0, false, 10, true));

        // ]0;5]U[5;10[=]0;10[
        assertEquals(i(0, false, 5, true).union(i(5, true, 10, false)), i(0, false, 10, false));

        // [0;5[U]5;10]=[0;5[U]5;10]
        assertEquals(i(0, false, 5, false).union(i(5, 10)), i(0, false, 10, false));
    }

    @Test
    void intersect() {
    }
}