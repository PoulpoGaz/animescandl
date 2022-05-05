package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public interface SearchWebsite<S> extends Website {

    FilterList getSearchFilter();

    List<S> search(String search, FilterList filter)
            throws IOException, InterruptedException, WebsiteException, JsonException;
}
