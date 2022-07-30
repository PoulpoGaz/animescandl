package fr.poulpogaz.animescandl.utils.math;

import fr.poulpogaz.animescandl.utils.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SetParserTest {

    @Test
    void test() throws ParseException {
        SetParser parser = new SetParser();

        Set s = parser.parse("0 - 10, 20 - 50, 15");
        assertEquals(
                new Union(Interval.closed(0, 10), new Singleton(15), Interval.closed(20, 50)),
                s);

        assertThrows(ParseException.class, () -> parser.parse("0--,"));
        assertThrows(ParseException.class, () -> parser.parse("0,"));
        assertThrows(ParseException.class, () -> parser.parse("0,,"));
        assertThrows(ParseException.class, () -> parser.parse("50-30"));
        assertThrows(ParseException.class, () -> parser.parse("hello world!"));

        parser.setSetFactory(new SetParser.DefaultFactory() {
            @Override
            public Set singleton(float a) {
                return Interval.closedOpen(a, a + 1);
            }

            @Override
            public Set interval(float min, float max) {
                return Interval.closedOpen(min, max + 1);
            }
        });

        s = parser.parse("1-2,50-60, 3");
        assertEquals(
                new Union(Interval.closedOpen(1, 4), Interval.closedOpen(50, 61)),
                s);

        assertEquals(Interval.greaterThan(0), parser.parse("0-"));
        assertEquals(Interval.lessThan(0), parser.parse("-0"));
        assertEquals(new Union(Interval.lessThan(0), Interval.greaterThan(1)), parser.parse("-0,1-"));
        assertEquals(Interval.all(), parser.parse("-0,1-,-,55"));
    }
}
