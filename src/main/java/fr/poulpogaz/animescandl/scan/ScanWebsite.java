package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public interface ScanWebsite<M extends Manga, C extends Chapter> extends Website {

    boolean isChapterURL(String url);

    boolean isMangaURL(String url);

    default boolean isSupported(String url) {
        return isChapterURL(url) || isMangaURL(url);
    }

    M getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    List<C> getChapters(M manga)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    Class<?>[] supportedIterators();

    <P> PageIterator<P> getPageIterator(C chapter, Class<P> out)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    default boolean supportLanguage() {
        return false;
    }

    default void selectLanguage(String language) {

    }

    List<M> search();
}
