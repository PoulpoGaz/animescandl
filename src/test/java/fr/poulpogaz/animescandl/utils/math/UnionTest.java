package fr.poulpogaz.animescandl.utils.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnionTest {

    @Test
    void constructorTest() {
        assertDoesNotThrow(() -> {
            Interval i = Interval.closed(-10, -4);
            Interval i2 = Interval.open(-4, -2);
            Singleton s = new Singleton(-1);
            Set a = new Singleton(0);
            Set b = new Singleton(1);

            Union u = new Union(b, a, i);
            assertEquals(i, u.getSets().get(0));
            assertEquals(a, u.getSets().get(1));
            assertEquals(b, u.getSets().get(2));


            Union u2 = new Union(s, i2);
            assertEquals(i2, u2.getSets().get(0));
            assertEquals(s, u2.getSets().get(1));

            Union superUnion = new Union(u, u2);
            assertEquals(i, superUnion.getSets().get(0));
            assertEquals(i2, superUnion.getSets().get(1));
            assertEquals(s, superUnion.getSets().get(2));
            assertEquals(a, superUnion.getSets().get(3));
            assertEquals(b, superUnion.getSets().get(4));
        });
    }
}
