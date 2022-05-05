package fr.poulpogaz.animescandl.website.filter;

import java.util.List;

public class Group extends Filter<List<Filter<?>>> {

    public Group(String name, List<Filter<?>> filters) {
        super(name, filters);

        if (filters.isEmpty()) {
            throw new IllegalArgumentException("Empty group");
        }

        for (Filter<?> f : filters) {
            if (f instanceof Group) {
                throw new IllegalArgumentException("A group can't contains a group");
            }
        }
    }

    @Override
    public void setValue(List<Filter<?>> value) {
        throw new IllegalStateException();
    }

    public List<Filter<?>> getFilters() {
        return super.getValue();
    }

    public Filter<?> getFilter(String name) {
        for (Filter<?> filter : getFilters()) {
            if (name.equals(filter.getName())) {
                return filter;
            }
        }

        return null;
    }
}
