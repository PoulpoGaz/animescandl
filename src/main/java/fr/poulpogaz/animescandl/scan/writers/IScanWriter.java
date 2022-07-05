package fr.poulpogaz.animescandl.scan.writers;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IScanWriter {

    /**
     * Writes a single chapter.
     * Does not check if the chapter has already been written
     */
    default void writeScan(ScanWebsite website, Chapter chapter, Path out)
            throws IOException, JsonException, WebsiteException, InterruptedException {
        writeScans(website, List.of(chapter), out);
    }

    /**
     * Writes all chapters in the same file.
     * Does not check if the chapters has already been written
     */
    void writeScans(ScanWebsite website, List<Chapter> chapters, Path out)
            throws IOException, JsonException, WebsiteException, InterruptedException;
}
