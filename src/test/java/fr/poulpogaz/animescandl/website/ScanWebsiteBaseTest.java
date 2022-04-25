package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * This class doesn't check if the manga has correct information
 * or the iterator returns valid url.
 * It only aims at not throwing exceptions.
 */
public abstract class ScanWebsiteBaseTest<M extends Manga, C extends Chapter> {

    protected void test(String baseURL)
            throws WebsiteException, IOException, InterruptedException,
            JsonException, UnsupportedPlatformException, CefInitializationException {
        ScanWebsite<M, C> sw = getScanWebsite();

        if (GraphicsEnvironment.isHeadless() && !sw.supportHeadless()) {
            throw new WebsiteException("Test can't be run because "
                    + sw.name() + " doesn't support headless");
        }

        if (needCEF()) {
            CEFHelper.initialize();
        }

        M m = sw.getManga(baseURL);

        List<C> chapters = sw.getChapters(m);
        assertFalse(chapters.isEmpty());

        PageIterator<String> iterator = sw.getPageIterator(chapters.get(0), String.class);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void mangaTest() throws WebsiteException, IOException, InterruptedException,
            JsonException, UnsupportedPlatformException, CefInitializationException {
        test(getManga());
    }

    protected abstract String getManga();

    @Test
    public void chapterTest() throws JsonException, WebsiteException, IOException,
            InterruptedException, UnsupportedPlatformException, CefInitializationException {
        test(getChapter());
    }

    protected abstract String getChapter();

    @Test
    public void unknownURL() {
        assertThrows(UnsupportedURLException.class, () -> test(getScanWebsite().url()));
    }

    /*@AfterAll
    public static void shutdown() {
        CEFHelper.shutdown();
    }*/

    protected boolean needCEF() {
        return false;
    }

    protected abstract ScanWebsite<M, C> getScanWebsite();
}
