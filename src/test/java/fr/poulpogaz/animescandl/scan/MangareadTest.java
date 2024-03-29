package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.CheckBox;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.Select;
import fr.poulpogaz.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class MangareadTest extends ScanWebsiteBaseTest {

    private static final MangaRead mr = new MangaRead();

    @Override
    protected String getManga() {
        return "https://www.mangaread.org/manga/berserk/";
    }

    @Override
    protected String getChapter() {
        return "https://www.mangaread.org/manga/berserk/chapter-0/";
    }

    @Override
    protected ScanWebsite getScanWebsite() {
        return mr;
    }

    @Test
    void search() throws JsonException, IOException, WebsiteException, InterruptedException {
        FilterList fl = mr.getSearchFilter();
        fl.setLimit(20);
        fl.setOffset(15);

        ((Select<?>) fl.getFilter("Order")).setValue(2);
        ((CheckBox) fl.getFilter("Action")).select();
        ((CheckBox) fl.getFilter("Adventure")).select();

        List<Manga> mangas = mr.search("a", fl);

        System.out.println(mangas.size());
        mangas.forEach(System.out::println);
    }
}
