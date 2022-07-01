package fr.poulpogaz.animescandl.scan.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.Japanread;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.scan.ScanWebsiteBaseTest;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.*;
import fr.poulpogaz.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class MangadexTest extends ScanWebsiteBaseTest<Manga, Chapter> {

    private static final Mangadex md = new Mangadex();

    @Override
    protected String getManga() {
        return "https://mangadex.org/title/801513ba-a712-498c-8f57-cae55b38cc92/berserk";
    }

    @Override
    protected String getChapter() {
        return "https://mangadex.org/chapter/6310f6a1-17ee-4890-b837-2ec1b372905b/1";
    }

    @Override
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        return md;
    }

    @Test
    void search() throws JsonException, IOException, WebsiteException, InterruptedException {
        FilterList fl = md.getSearchFilter();
        fl.setLimit(10);
        fl.setOffset(1);

        ((CheckBox) fl.getFilter("Shounen")).select();
        ((CheckBox) fl.getFilter("Ongoing")).select();
        ((TriStateCheckBox) fl.getFilter("Sexual Violence")).exclude();
        ((TriStateCheckBox) fl.getFilter("Slice of Life")).select();
        ((TriStateCheckBox) fl.getFilter("Harem")).select();
        ((TriStateCheckBox) fl.getFilter("Incest")).exclude();
        ((CheckBox) fl.getFilter("Japanese")).select();
        ((Select<?>) fl.getFilter("Sort By")).setValue(1);

        List<Manga> mangas = md.search("a", fl);

        System.out.println(mangas.size());
        mangas.forEach(System.out::println);
    }
}