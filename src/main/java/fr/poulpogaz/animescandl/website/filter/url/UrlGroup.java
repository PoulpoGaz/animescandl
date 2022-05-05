package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.website.filter.Filter;
import fr.poulpogaz.animescandl.website.filter.Group;

import java.util.List;

public class UrlGroup extends Group implements UrlFilter {

    private final String queryName;

    public UrlGroup(String name, List<Filter<?>> filters, String queryName) {
        super(name, filters);
        this.queryName = queryName;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQueryArgument() {
        return null;
    }
}
