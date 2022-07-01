package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public interface ScanWebsite extends Website {

    boolean isChapterURL(String url);

    boolean isMangaURL(String url);

    default boolean isSupported(String url) {
        return isChapterURL(url) || isMangaURL(url);
    }

    Manga getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    List<Chapter> getChapters(Manga manga)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    Class<?>[] supportedIterators();

    <P> PageIterator<P> getPageIterator(Chapter chapter, Class<P> out)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    default boolean supportLanguage() {
        return false;
    }

    default void selectLanguage(String language) {

    }
}
