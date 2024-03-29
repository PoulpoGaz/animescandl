package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
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
public abstract class ScanWebsiteBaseTest {

    protected void test(String baseURL)
            throws WebsiteException, IOException, InterruptedException,
            JsonException, UnsupportedPlatformException, CefInitializationException {
        ScanWebsite sw = getScanWebsite();

        if (GraphicsEnvironment.isHeadless() && !sw.needCEF()) {
            throw new WebsiteException("Test can't be run because "
                    + sw.name() + " doesn't support headless");
        }

        if (sw.needCEF()) {
            CEFHelper.initialize();
        }

        Manga m = sw.getManga(baseURL);

        List<Chapter> chapters = sw.getChapters(m);
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

    protected abstract ScanWebsite getScanWebsite();
}
