package fr.poulpogaz.animescandl.scan.writers;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Manga;

public class ScanWriters {

    private static final Simulate SIMULATE = new Simulate();

    public static IScanWriter newWriter(Manga manga, boolean continueDownload) {
        if (Main.simulate.isPresent()) {
            return SIMULATE;
        } else {
            return new PDFScanWriter(manga, continueDownload);
        }
    }
}
