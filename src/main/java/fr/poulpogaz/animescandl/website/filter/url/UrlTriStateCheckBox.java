package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.TriStateCheckBox;

public class UrlTriStateCheckBox extends TriStateCheckBox implements UrlFilter {

    private final String queryName;
    private final String valueWhenSelected;
    private final String valueWhenUnselected;
    private final String valueWhenExcluded;

    public UrlTriStateCheckBox(String name,
                               String queryName,
                               String valueWhenSelected,
                               String valueWhenUnselected,
                               String valueWhenExcluded) {
        super(name);
        this.queryName = queryName;
        this.valueWhenSelected = valueWhenSelected;
        this.valueWhenUnselected = valueWhenUnselected;
        this.valueWhenExcluded = valueWhenExcluded;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQueryArgument() {
        return switch (value) {
            case 0 -> valueWhenUnselected;
            case 1 -> valueWhenSelected;
            case 2 -> valueWhenExcluded;
            default -> throw new IllegalStateException("Value must be 0, 1 or 2");
        };
    }

    public String getValueWhenSelected() {
        return valueWhenSelected;
    }

    public String getValueWhenUnselected() {
        return valueWhenUnselected;
    }

    public String getValueWhenExcluded() {
        return valueWhenExcluded;
    }
}
