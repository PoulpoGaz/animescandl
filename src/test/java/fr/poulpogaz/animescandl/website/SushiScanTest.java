package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;

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
}
