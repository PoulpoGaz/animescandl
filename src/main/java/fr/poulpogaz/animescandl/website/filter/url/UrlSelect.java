package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.Select;

import java.util.List;

public class UrlSelect<V> extends Select<V> implements UrlFilter {

    private final String queryName;
    private final List<String> arguments;

    public UrlSelect(String name, List<V> acceptedValues, String queryName, List<String> arguments) {
        super(name, acceptedValues);
        this.queryName = queryName;
        this.arguments = arguments;

        if (arguments.size() != acceptedValues.size()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQueryArgument() {
        if (value == null) {
            return null;
        }

        return arguments.get(value);
    }
}
