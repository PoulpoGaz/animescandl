package fr.poulpogaz.animescandl.utils.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathTestUtils {

    public static void intersectionTest(Set a, Set b, Set expected) {
        assertEquals(expected, a.intersect(b));
        assertEquals(expected, b.intersect(a));
        assertEquals(a, a.intersect(a));
        assertEquals(b, b.intersect(b));
    }

    public static void unionTest(Set a, Set b, Set expected) {
        assertEquals(expected, a.union(b));
        assertEquals(expected, b.union(a));
        assertEquals(a, a.union(a));
        assertEquals(b, b.union(b));
    }
}
