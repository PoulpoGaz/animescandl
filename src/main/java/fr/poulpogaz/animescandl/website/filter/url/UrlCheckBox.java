package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.CheckBox;

public class UrlCheckBox extends CheckBox implements UrlFilter {

    private final String queryName;
    private final String valueWhenUnselected;
    private final String valueWhenSelected;

    public UrlCheckBox(String name, String queryName, String valueWhenUnselected, String valueWhenSelected) {
        super(name);
        this.queryName = queryName;
        this.valueWhenUnselected = valueWhenUnselected;
        this.valueWhenSelected = valueWhenSelected;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQueryArgument() {
        return isSelected() ? valueWhenSelected : valueWhenUnselected;
    }

    public String getValueWhenSelected() {
        return valueWhenSelected;
    }

    public String getValueWhenUnselected() {
        return valueWhenUnselected;
    }
}
