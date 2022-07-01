package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.TriStateCheckBox;

public class ListTriStateCheckBox extends TriStateCheckBox implements UrlFilter {

    private final String selected;
    private final String excluded;
    private final String id;

    public ListTriStateCheckBox(String name, String selected, String excluded, String id) {
        super(name);
        this.selected = selected;
        this.excluded = excluded;
        this.id = id;
    }

    @Override
    public String getQueryName() {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case SELECTED -> selected;
            case EXCLUDE -> excluded;
            default -> null;
        };
    }

    @Override
    public String getQueryArgument() {
        if (value != null && (value == SELECTED || value == EXCLUDE)) {
            return id;
        }

        return null;
    }
}
