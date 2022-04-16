package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.SushiScan;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;

import java.io.IOException;
import java.util.List;

public class Main2 {

    public static void main(String[] args) throws WebsiteException, IOException, InterruptedException {
        SushiScan su = new SushiScan();

        Manga manga = su.getManga("https://sushi-scan.su/hunter-x-hunter-volume-30/");
        System.out.println(manga);

        List<Chapter> chapters = su.getChapters(manga);

        Chapter vol30 = chapters.stream()
                .filter((c) -> c.getChapterNumber() == 30).
                findAny()
                .orElseThrow();
        System.out.println(vol30);

        PageIterator<String> iterator = su.getPageIterator(vol30, String.class);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
