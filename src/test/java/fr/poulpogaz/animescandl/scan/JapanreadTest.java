package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.MangaWithChapter;

import java.io.IOException;

public class JapanreadTest extends ScanWebsiteBaseTest<MangaWithChapter, Chapter> {

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
    protected ScanWebsite<MangaWithChapter, Chapter> getScanWebsite() {
        if (jr == null) {
            try {
                jr = new Japanread();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize Japanread");
            }
        }

        return jr;
    }
}
