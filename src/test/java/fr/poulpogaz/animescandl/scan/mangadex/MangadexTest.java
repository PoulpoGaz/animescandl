package fr.poulpogaz.animescandl.scan.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.website.ScanWebsiteBaseTest;

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
}