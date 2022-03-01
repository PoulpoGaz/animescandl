package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.website.AbstractWebsite;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractScanWriter {

    public static AbstractScanWriter newWriter(String title, boolean concatenateAll, Path out) {
        if (AbstractWebsite.NO_DOWNLOAD) {
            return Simulate.INSTANCE;
        } else {
            return new ScanWriter(title, concatenateAll, out);
        }
    }

    /** The title of the series. eg: bla_bla */
    protected final String title;
    protected final boolean concatenateAll;

    public AbstractScanWriter(String title, boolean concatenateAll) {
        this.title = title;
        this.concatenateAll = concatenateAll;
    }

    public abstract void newScan(String name);

    public abstract void addPage(BufferedImage image) throws IOException;

    public abstract void addPage(byte[] data) throws IOException;

    public abstract void addPage(IRequestSender s, String page) throws IOException, InterruptedException;

    public abstract void endScan() throws IOException;

    public abstract void endAll() throws IOException;

    private static final class Simulate extends AbstractScanWriter {

        private static final Simulate INSTANCE = new Simulate(null, false);

        private Simulate(String title, boolean concatenateAll) {
            super(title, concatenateAll);
        }

        @Override
        public void newScan(String name) {

        }

        @Override
        public void addPage(BufferedImage image) {

        }

        @Override
        public void addPage(byte[] data) throws IOException {

        }

        @Override
        public void addPage(IRequestSender s, String page) throws IOException, InterruptedException {

        }

        @Override
        public void endScan() throws IOException {

        }

        @Override
        public void endAll() throws IOException {

        }
    }
}
