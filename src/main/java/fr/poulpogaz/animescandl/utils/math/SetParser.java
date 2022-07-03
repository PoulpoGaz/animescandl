package fr.poulpogaz.animescandl.utils.math;

import fr.poulpogaz.animescandl.utils.ParseException;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Allow only positive values.
 *
 * Valid values:
 * 1,2,3-10,4-10s
 *
 */
public class SetParser {

    // start of interval/singleton
    private static final int EXPECT_NUMBER = 0;

    // after start
    private static final int EXPECT_COMMA_OR_HYPHEN = 1;

    // after hyphen, expecting number
    private static final int INTERVAL_END = 2;

    // after interval end, expecting comma
    private static final int EXPECT_COMMA = 3;

    private BiFunction<Float, Float, Set> intervalCreator = Interval::closed;
    private Function<Float, Set> singletonCreator = Singleton::new;


    private String str;
    private int status;
    private int pos;

    private Set output;
    private float start;

    public SetParser() {

    }

    public Set parse(String str) throws ParseException {
        this.str = str;
        status = EXPECT_NUMBER;
        pos = 0;
        output = Empty.INSTANCE;
        start = 0;

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if (Character.isWhitespace(c)) {
                continue;
            }

            parseChar(c);
        }

        if (status == EXPECT_COMMA_OR_HYPHEN) {
            output = output.union(singletonCreator.apply(start));

        } if (status == INTERVAL_END || status == EXPECT_NUMBER) {
            throw new ParseException("Expecting number but EOS");
        }

        return output;
    }

    private void parseChar(char c) throws ParseException {
        if (status == EXPECT_NUMBER) {
            start = nextNumber();
            status = EXPECT_COMMA_OR_HYPHEN;

        } else if (status == EXPECT_COMMA_OR_HYPHEN) {
            if (c == ',') {
                output = output.union(singletonCreator.apply(start));
                status = EXPECT_NUMBER;
            } else if (c == '-') {
                status = INTERVAL_END;
            } else {
                throw new ParseException("Expecting a comma or a hyphen");
            }

        } else if (status == INTERVAL_END) {
            float end = nextNumber();

            if (start > end) {
                throw new ParseException("Invalid range: " + start + "-" + end);
            } else if (start == end) {
                output = output.union(singletonCreator.apply(start));
            } else {
                output = output.union(intervalCreator.apply(start, end));
            }

            status = EXPECT_COMMA;
        } else if (status == EXPECT_COMMA) {
            if (c == ',') {
                status = EXPECT_NUMBER;
            } else {
                throw new ParseException("Expecting comma after interval");
            }

        } else {
            throw new RuntimeException("Unknown state: " + status);
        }
    }

    private float nextNumber() throws ParseException {
        StringBuilder number = new StringBuilder();

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if ((c >= '0' && c <= '9') || c == '.') {
                number.append(c);
            } else {
                pos--;
                break;
            }
        }

        try {
            return Float.parseFloat(number.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Not a number: " + number, e);
        }
    }

    public void setIntervalCreator(BiFunction<Float, Float, Set> intervalCreator) {
        this.intervalCreator = Objects.requireNonNull(intervalCreator);
    }

    public void setSingletonCreator(Function<Float, Set> singletonCreator) {
        this.singletonCreator = Objects.requireNonNull(singletonCreator);
    }
}
