package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.TriStateCheckBox;

public class UrlTriStateCheckBox extends TriStateCheckBox implements UrlFilter {

    private final String queryNameWhenUnselected;
    private final String valueWhenUnselected;

    private final String queryNameWhenSelected;
    private final String valueWhenSelected;

    private final String queryNameWhenExcluded;
    private final String valueWhenExcluded;

    public UrlTriStateCheckBox(String name,
                               String queryName,
                               String valueWhenUnselected,
                               String valueWhenSelected,
                               String valueWhenExcluded) {
        this(name,
                queryName, valueWhenUnselected,
                queryName, valueWhenSelected,
                queryName, valueWhenExcluded);
    }

    public UrlTriStateCheckBox(String name,
                               String valueWhenUnselected,
                               String queryNameWhenSelected,
                               String valueWhenSelected,
                               String queryNameWhenExcluded,
                               String valueWhenExcluded) {
        this(name,
                null, valueWhenUnselected,
                queryNameWhenSelected, valueWhenSelected,
                queryNameWhenExcluded, valueWhenExcluded);
    }

    public UrlTriStateCheckBox(String name,
                               String queryNameWhenUnselected,
                               String valueWhenUnselected,
                               String queryNameWhenSelected,
                               String valueWhenSelected,
                               String queryNameWhenExcluded,
                               String valueWhenExcluded) {
        super(name);
        this.queryNameWhenUnselected = queryNameWhenUnselected;
        this.valueWhenUnselected = valueWhenUnselected;
        this.queryNameWhenSelected = queryNameWhenSelected;
        this.valueWhenSelected = valueWhenSelected;
        this.queryNameWhenExcluded = queryNameWhenExcluded;
        this.valueWhenExcluded = valueWhenExcluded;
    }

    @Override
    public String getQueryName() {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case 0 -> queryNameWhenUnselected;
            case 1 -> queryNameWhenSelected;
            case 2 -> queryNameWhenExcluded;
            default -> throw new IllegalStateException("Value must be 0, 1 or 2");
        };
    }

    @Override
    public String getQueryArgument() {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case 0 -> valueWhenUnselected;
            case 1 -> valueWhenSelected;
            case 2 -> valueWhenExcluded;
            default -> throw new IllegalStateException("Value must be 0, 1 or 2");
        };
    }

    public String getQueryNameWhenUnselected() {
        return queryNameWhenUnselected;
    }

    public String getValueWhenUnselected() {
        return valueWhenUnselected;
    }

    public String getQueryNameWhenSelected() {
        return queryNameWhenSelected;
    }

    public String getValueWhenSelected() {
        return valueWhenSelected;
    }

    public String getQueryNameWhenExcluded() {
        return queryNameWhenExcluded;
    }

    public String getValueWhenExcluded() {
        return valueWhenExcluded;
    }
}
