package fr.poulpogaz.animescandl.utils;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface IDocumentCache {

    void clearCache();

    Document getDocument(String url) throws IOException, InterruptedException ;

    int getCacheLimit();

    void setCacheLimit(int limit);
}
