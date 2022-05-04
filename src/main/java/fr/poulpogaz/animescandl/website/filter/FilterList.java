package fr.poulpogaz.animescandl.website.filter;

import java.util.List;

public class FilterList {

    private final List<Filter<?>> filters;
    private int limit = 10;

    public FilterList(List<Filter<?>> filters) {
        this.filters = filters;
    }

    public Filter<?> get(int index) {
        return filters.get(index);
    }

    public int getLimit() {
        return limit;
    }

    public FilterList setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public List<Filter<?>> getFilters() {
        return filters;
    }
}
