package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.ScanWriter;
import fr.poulpogaz.animescandl.website.AbstractScanWebsite;
import fr.poulpogaz.animescandl.website.ScanWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.animescandl.website.japscan.Japscan;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2 {

    public static void main(String[] args)
            throws WebsiteException, IOException, InterruptedException,
            JsonException, UnsupportedPlatformException, CefInitializationException {
        // testString(new SushiScan(), "https://sushi-scan.su/hunter-x-hunter-volume-30/");
        // testString(new Mangadex(), "https://mangadex.org/title/a96676e5-8ae2-425e-b549-7f15dd34a6d8/komi-san-wa-komyushou-desu");
        // testString(new MangaRead(), "https://www.mangaread.org/manga/we-never-learn/");

        CEFHelper.initialize();
        testString(new Japscan(), "https://www.japscan.ws/lecture-en-ligne/elfen-lied/volume-1/10.html");
        CEFHelper.shutdown();
    }

    private static <M extends Manga, C extends Chapter>
    void testString(AbstractScanWebsite<M, C> w, String url)
            throws WebsiteException, IOException, InterruptedException, JsonException {
        M manga = w.getManga(url);
        System.out.println(manga);

        List<C> chapters = w.getChapters(manga);
        System.out.println(chapters);

        C chap = chapters.stream()
                .filter((c) -> c.getChapterNumber() == 10).
                findAny()
                .orElseThrow();
        System.out.println(chap);

        ScanWriter sw = new ScanWriter(manga.getTitle(), false, Path.of(""));
        PageIterator<String> iterator = w.getPageIterator(chap, String.class);

        sw.newScan(chap.getName().orElse(manga.getTitle() + " - " + chap.getChapterNumber()));
        while (iterator.hasNext()) {
            String next = iterator.next();
            sw.addPage(w, next);
        }
        sw.endScan();
        sw.endAll();
    }
}
