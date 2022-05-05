package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.CheckBox;

public class UrlCheckBox extends CheckBox implements UrlFilter {

    private final String queryName;
    private final String valueWhenSelected;
    private final String valueWhenUnselected;

    public UrlCheckBox(String name, String queryName, String valueWhenSelected, String valueWhenUnselected) {
        super(name);
        this.queryName = queryName;
        this.valueWhenSelected = valueWhenSelected;
        this.valueWhenUnselected = valueWhenUnselected;
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
