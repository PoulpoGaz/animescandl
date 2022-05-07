package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.scan.ScanWebsiteBaseTest;
import fr.poulpogaz.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class JapscanTest extends ScanWebsiteBaseTest<Manga, Chapter> {

    private static final Japscan js = new Japscan();

    @Override
    protected String getManga() {
        return "https://www.japscan.ws/manga/berserk/";
    }

    @Override
    protected String getChapter() {
        return "https://www.japscan.ws/lecture-en-ligne/berserk/volume-1/";
    }

    @Override
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        return js;
    }

    @Test
    void searchTest() throws JsonException, IOException, InterruptedException {
        List<Manga> mangas = js.search("test", null);

        System.out.println(mangas.size());
        mangas.forEach(System.out::println);
    }
}