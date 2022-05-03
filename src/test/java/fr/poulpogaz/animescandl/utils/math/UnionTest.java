package fr.poulpogaz.animescandl.utils.math;

import org.junit.jupiter.api.Test;

import static fr.poulpogaz.animescandl.utils.math.MathTestUtils.intersectionTest;
import static fr.poulpogaz.animescandl.utils.math.MathTestUtils.unionTest;
import static org.junit.jupiter.api.Assertions.*;

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


            Union u2 = new Union(s, i2, Empty.INSTANCE);
            assertEquals(i2, u2.getSets().get(0));
            assertEquals(s, u2.getSets().get(1));

            Union superUnion = new Union(u, u2);
            assertEquals(i, superUnion.getSets().get(0));
            assertEquals(i2, superUnion.getSets().get(1));
            assertEquals(s, superUnion.getSets().get(2));
            assertEquals(a, superUnion.getSets().get(3));
            assertEquals(b, superUnion.getSets().get(4));
        });

        assertThrows(IllegalArgumentException.class, Union::new);
        assertThrows(IllegalArgumentException.class, () -> {
            Interval i = Interval.closed(-10, -4);
            Interval i2 = Interval.closed(-4, -2);

            new Union(i, i2);
        });
    }

    @Test
    void union() {
        // [0; 1] U [2; 3]
        // [1; 2]
        unionTest(
                new Union(Interval.closed(0, 1), Interval.closed(2, 3)),
                Interval.closed(1, 2),
                Interval.closed(0, 3)
        );

        // [0; 1] U [2; 3] U [4; 5]
        // [1; 2]
        unionTest(
                new Union(Interval.closed(0, 1), Interval.closed(2, 3), Interval.closed(4, 5)),
                Interval.closed(1, 2),
                new Union(Interval.closed(0, 3), Interval.closed(4, 5))
        );

        // [0; 10] U [20; 30]
        // [1; 2] U ]3; 4] U [21; 22[ U ]23; 24[
        unionTest(
                new Union(Interval.closed(0, 10), Interval.closed(20, 30)),
                new Union(Interval.closed(1, 2), Interval.openClosed(3, 4), Interval.closedOpen(21, 22), Interval.open(23, 24)),
                new Union(Interval.closed(0, 10), Interval.closed(20, 30))
        );

        // ]-inf; 0] U [10; 15]
        // [20; 30] U [40; 50]
        unionTest(
                new Union(Interval.lessThan(0), Interval.closed(10, 15)),
                new Union(Interval.closed(20, 30), Interval.closed(40, 50)),
                new Union(Interval.closed(20, 30), Interval.closed(40, 50), Interval.lessThan(0), Interval.closed(10, 15))
        );
    }

    @Test
    void intersect() {
        // [1;2[ U ]3;4]
        // [2;3]
        intersectionTest(
                new Union(Interval.closedOpen(1, 2), Interval.openClosed(3, 4)),
                Interval.closed(2, 3),
                Empty.INSTANCE);

        // ]1; 3.25[ U ]3.5; 10[
        // [2;3] U [4;5[
        intersectionTest(
                new Union(Interval.open(1, 3.25f), Interval.open(3.5f, 10)),
                new Union(Interval.closed(2, 3), Interval.closedOpen(4, 5)),
                new Union(Interval.closed(2, 3), Interval.closedOpen(4, 5)));

        // ]-10; 10] U {15}
        // R\{0}
        intersectionTest(
                new Union(Interval.openClosed(-10, 10), new Singleton(15)),
                new Union(Interval.strictGreaterThan(0), Interval.strictLessThan(0)),
                new Union(Interval.open(-10, 0), Interval.openClosed(0, 10), new Singleton(15))
        );

        // [0; 2[ U ]3; 8]
        // [1; 2] U [4; 5] U {8}
        intersectionTest(
                new Union(Interval.closedOpen(0, 2), Interval.openClosed(3, 8)),
                new Union(Interval.closed(1, 2), Interval.closed(4, 5), new Singleton(8)),
                new Union(Interval.closedOpen(1, 2), Interval.closed(4, 5), new Singleton(8))
        );

        // ]0; 100] U [200; 300]
        // ]0; 10] U [20; 30] U [75; 225] U [250; +inf[
        intersectionTest(
                new Union(Interval.openClosed(0, 100), Interval.closed(200, 300)),
                new Union(Interval.openClosed(0, 10), Interval.closed(20, 30), Interval.closed(75, 225), Interval.greaterThan(250)),
                new Union(Interval.openClosed(0, 10), Interval.closed(20, 30), Interval.closed(75, 100), Interval.closed(200, 225), Interval.closed(250, 300))
        );
    }
}
