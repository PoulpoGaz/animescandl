package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public interface ScanWebsite<M extends Manga, C extends Chapter> extends Website {

    String name();

    String url();

    String version();

    boolean isChapterURL(String url);

    boolean isMangaURL(String url);

    default boolean isSupported(String url) {
        return isChapterURL(url) || isMangaURL(url);
    }

    M getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    /**
     * Chapters are ordered by the chapter number
     */
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
