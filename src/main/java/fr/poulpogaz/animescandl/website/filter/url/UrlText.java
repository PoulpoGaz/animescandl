package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.Text;

public class UrlText extends Text implements UrlFilter {

    private final String queryName;

    public UrlText(String name, String queryName) {
        super(name);
        this.queryName = queryName;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQueryArgument() {
        return getValue();
    }
}
