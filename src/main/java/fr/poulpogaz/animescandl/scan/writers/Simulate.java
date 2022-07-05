package fr.poulpogaz.animescandl.scan.writers;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

final class Simulate implements IScanWriter {

    @Override
    public void writeScans(ScanWebsite website, List<Chapter> chapters, Path out)
            throws IOException, JsonException, WebsiteException, InterruptedException {

    }
}