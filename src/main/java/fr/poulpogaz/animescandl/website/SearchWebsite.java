package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.website.filter.FilterList;

import java.util.List;

public interface SearchWebsite<S> extends Website {

    FilterList getSearchFilter();

    List<S> search(String search, FilterList filter);
}
