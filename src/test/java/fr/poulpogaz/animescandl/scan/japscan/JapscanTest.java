package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.website.ScanWebsiteBaseTest;

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
    protected boolean needCEF() {
        return true;
    }

    @Override
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        return js;
    }
}