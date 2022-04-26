package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.model.*;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.utils.ScanWriter;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main2 {

    public static void main(String[] args)
            throws WebsiteException, IOException, InterruptedException,
            JsonException, UnsupportedPlatformException, CefInitializationException {
       //try {
       //    // scanTest(new SushiScan(), "https://sushi-scan.su/hunter-x-hunter-volume-30/");
       //    // scanTest(new Mangadex(), "https://mangadex.org/title/a96676e5-8ae2-425e-b549-7f15dd34a6d8/komi-san-wa-komyushou-desu");
       //    // scanTest(new MangaRead(), "https://www.mangaread.org/manga/we-never-learn/");

       //    // CEFHelper.initialize();
       //    // scanTest(new Japscan(), "https://www.japscan.ws/lecture-en-ligne/elfen-lied/volume-1/10.html");
       //    // CEFHelper.shutdown();

       //    CEFHelper.initialize();
       //    scanTest(new Japanread(), "https://www.japanread.cc/manga/the-god-of-high-school/1", false);
       //} finally {
       //    CEFHelper.shutdown();
       //}

        animeTest(new Nekosama(), "https://neko-sama.fr/anime/info/4973-steins-gate-vostfr", false);
    }

    private static <M extends Manga, C extends Chapter>
    void scanTest(ScanWebsite<M, C> w, String url, boolean write)
            throws WebsiteException, IOException, InterruptedException, JsonException {
        M manga = w.getManga(url);
        System.out.println(manga);

        List<C> chapters = w.getChapters(manga);

        C chap = chapters.stream()
                .filter((c) -> c.getChapterNumber() == 10).
                findAny()
                .orElseThrow();
        System.out.println(chap);

        PageIterator<String> iterator = w.getPageIterator(chap, String.class);

        if (write) {
            ScanWriter sw = new ScanWriter(manga.getTitle(), false, Path.of(""));

            sw.newScan(chap.getName().orElse(manga.getTitle() + " - " + chap.getChapterNumber()));
            while (iterator.hasNext()) {
                String next = iterator.next();

                if (w instanceof IRequestSender s) {
                    sw.addPage(s, next);
                } else {
                    sw.addPage(HttpUtils.STANDARD, next);
                }
            }
            sw.endScan();
            sw.endAll();
        } else {
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
        }
    }

    private static <A extends Anime, E extends Episode>
    void animeTest(AnimeWebsite<A, E> a, String url, boolean write)
            throws JsonException, IOException, WebsiteException, InterruptedException {
        A anime = a.getAnime(url);
        System.out.println(anime);

        List<E> episodes = a.getEpisodes(anime);

        E episode = episodes.stream()
                .filter((c) -> c.getEpisode() == 10).
                findAny()
                .orElseThrow();
        System.out.println(episode);

        List<Source> sources = a.getSources(episode);
        System.out.println(sources);
    }
}
