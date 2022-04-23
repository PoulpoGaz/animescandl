package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;

public class MangareadTest extends ScanWebsiteBaseTest<Manga, Chapter> {

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
    protected ScanWebsite<Manga, Chapter> getScanWebsite() {
        return mr;
    }
}
