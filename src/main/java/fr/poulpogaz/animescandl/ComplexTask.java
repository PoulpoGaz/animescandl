package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.ParseException;
import fr.poulpogaz.animescandl.utils.math.Interval;
import fr.poulpogaz.animescandl.utils.math.Set;
import fr.poulpogaz.animescandl.utils.math.SetParser;
import fr.poulpogaz.json.tree.JsonObject;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

public class ComplexTask extends Task {

    private static final SetParser PARSER;

    static {
        PARSER = new SetParser();
        PARSER.setIntervalCreator((a, b) -> Interval.closedOpen(a, b + 1));
        PARSER.setSingletonCreator((a) -> Interval.closedOpen(a, a + 1));
    }

    private String error;
    private String name;
    private boolean concatenateAll;
    private Set range;
    private String language;
    private Path out;

    public ComplexTask(JsonObject object, int number) {
        super(number);

        Optional<String> nameOpt = object.getOptionalString("name");

        if (nameOpt.isPresent()) {
            name = nameOpt.get();
        } else {
            error = "No name";
        }

        Optional<String> rangeOpt = object.getOptionalString("range");
        if (rangeOpt.isPresent()) {
            try {
                range = PARSER.parse(rangeOpt.get());
            } catch (ParseException e) {
                error = e.getMessage();
            }
        }

        concatenateAll = object.getOptionalBoolean("concatenateAll").orElse(false);

        language = object.getOptionalString("language").orElse(null);
        out = object.getOptionalString("out")
                .map((s) -> {
                    try {
                        return Path.of(s);
                    } catch (InvalidPathException e) {
                        error = "Invalid path";
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public boolean isValid() {
        return error == null;
    }

    @Override
    public String error() {
        return error;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean concatenateAll() {
        return concatenateAll;
    }

    @Override
    public Set range() {
        return range;
    }

    @Override
    public String language() {
        return language;
    }

    @Override
    public Path out() {
        return out;
    }
}
