package fr.poulpogaz.animescandl.utils.math;

import fr.poulpogaz.animescandl.args.ParseException;
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

        assertThrows(ParseException.class, () -> parser.parse("0-"));
        assertThrows(ParseException.class, () -> parser.parse("0--,"));
        assertThrows(ParseException.class, () -> parser.parse("0,"));
        assertThrows(ParseException.class, () -> parser.parse("0,,"));
        assertThrows(ParseException.class, () -> parser.parse("-0,"));
        assertThrows(ParseException.class, () -> parser.parse("50-30"));
        assertThrows(ParseException.class, () -> parser.parse("hello world!"));

        parser.setIntervalCreator((a, b) -> Interval.closedOpen(a, b + 1));
        parser.setSingletonCreator((a) -> Interval.closedOpen(a, a + 1));

        s = parser.parse("1-2,50-60, 3");
        assertEquals(
                new Union(Interval.closedOpen(1, 4), Interval.closedOpen(50, 61)),
                s);
    }
}
