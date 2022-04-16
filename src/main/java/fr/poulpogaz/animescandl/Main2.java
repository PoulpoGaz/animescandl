package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.MangaRead;
import fr.poulpogaz.animescandl.website.ScanWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public class Main2 {

    public static void main(String[] args)
            throws WebsiteException, IOException, InterruptedException, JsonException {
        // testString(new SushiScan(), "https://sushi-scan.su/hunter-x-hunter-volume-30/");
        // testString(new Mangadex(), "https://mangadex.org/title/a96676e5-8ae2-425e-b549-7f15dd34a6d8/komi-san-wa-komyushou-desu");
        testString(new MangaRead(), "https://www.mangaread.org/manga/we-never-learn/");
    }

    private static <M extends Manga, C extends Chapter>
    void testString(ScanWebsite<M, C> w, String url)
            throws WebsiteException, IOException, InterruptedException, JsonException {
        M manga = w.getManga(url);
        System.out.println(manga);

        List<C> chapters = w.getChapters(manga);

        C vol30 = chapters.stream()
                .filter((c) -> c.getChapterNumber() == 30).
                findAny()
                .orElseThrow();
        System.out.println(vol30);

        PageIterator<String> iterator = w.getPageIterator(vol30, String.class);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
