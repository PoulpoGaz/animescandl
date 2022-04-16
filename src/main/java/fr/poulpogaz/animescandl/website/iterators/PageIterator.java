package fr.poulpogaz.animescandl.website.iterators;

import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;
import java.util.Optional;

public interface PageIterator<T> {

    boolean hasNext();

    T next() throws IOException, InterruptedException, WebsiteException;

    Optional<Integer> nPages();
}
