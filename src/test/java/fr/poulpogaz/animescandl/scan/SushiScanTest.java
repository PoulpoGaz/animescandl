package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.CheckBox;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.Select;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class SushiScanTest extends ScanWebsiteBaseTest<Manga, Chapter> {

    private static final SushiScan su = new SushiScan();

    @Override
    protected String getManga() {
        return "https://sushi-scan.su/manga/berserk/";
    }

    @Override
    protected String getChapter() {
        return "https://sushi-scan.su/berserk-volume-1/";
    }

    @Override
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        return su;
    }

    @Test
    void search() throws IOException, WebsiteException, InterruptedException {
        FilterList fl = su.getSearchFilter();
        fl.setLimit(100);
        ((CheckBox) fl.getFilter("Action")).select();
        ((CheckBox) fl.getFilter("Aventure")).select();
        ((Select<?>) fl.getFilter("Statut")).setValue(1);
        ((Select<?>) fl.getFilter("Type")).setValue(1);
        ((Select<?>) fl.getFilter("Trier par")).setValue(5);

        List<Manga> mangas = su.search(null, fl);

        System.out.println(mangas.size());
        mangas.forEach((m) -> System.out.println(m.getTitle()));
    }
}
