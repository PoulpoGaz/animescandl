package fr.poulpogaz.animescandl.utils;

import org.jsoup.nodes.Document;

public interface IDocumentCache {

    void clearCache();

    Document getCachedDocument(String url);

    int getCacheLimit();

    void setCacheLimit(int limit);
}
