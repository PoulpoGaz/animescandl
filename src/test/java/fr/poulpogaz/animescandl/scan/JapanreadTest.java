package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.MangaWithChapter;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.*;
import fr.poulpogaz.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class JapanreadTest extends ScanWebsiteBaseTest<Manga, Chapter> {

    private static Japanread jr = null;

    @Override
    protected String getManga() {
        return "https://www.japanread.cc/manga/berserk";
    }

    @Override
    protected String getChapter() {
        return "https://www.japanread.cc/manga/berserk/358";
    }

    @Override
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        if (jr == null) {
            try {
                jr = new Japanread();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize Japanread");
            }
        }

        return jr;
    }

    @Test
    void search() throws JsonException, IOException, WebsiteException, InterruptedException {
        Japanread jr = (Japanread) getScanWebsite();

        FilterList fl = jr.getSearchFilter();
        fl.setLimit(20);
        fl.setOffset(15);

        ((TriStateCheckBox) fl.getFilter("Action")).select();
        ((TriStateCheckBox) fl.getFilter("Ecchi")).select();
        ((TriStateCheckBox) fl.getFilter("Harem")).select();
        ((TriStateCheckBox) fl.getFilter("Hentai")).exclude();
        ((TriStateCheckBox) fl.getFilter("Policier")).exclude();
        ((Text) fl.getFilter("Année de sortie")).setValue("2019");

        List<Manga> mangas = jr.search("a", fl);

        System.out.println(mangas.size());
        mangas.forEach(System.out::println);
    }
}
