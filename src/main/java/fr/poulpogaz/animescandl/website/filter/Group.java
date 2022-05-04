package fr.poulpogaz.animescandl.website.filter;

import java.util.List;

public class Group<V> extends Filter<List<Filter<V>>> {

    public Group(String name, List<Filter<V>> filters) {
        super(name, filters);
    }

    @Override
    public void setValue(List<Filter<V>> value) {
        throw new IllegalStateException();
    }
}
