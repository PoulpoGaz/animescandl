package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.Main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractScanWriter {

    public static AbstractScanWriter newWriter(String title, boolean concatenateAll, Path out) {
        if (Main.simulate.isPresent()) {
            return new Simulate(title, concatenateAll, out);
        } else {
            return new ScanWriter(title, concatenateAll, out);
        }
    }

    /** The title of the series. eg: bla_bla */
    protected final String title;
    protected final boolean concatenateAll;
    protected final Path out;

    /** The name of the current scan. eg: bla_bla_volume_1*/
    protected String name;

    public AbstractScanWriter(String title, boolean concatenateAll, Path out) {
        this.title = title;
        this.concatenateAll = concatenateAll;
        this.out = out;
    }

    public abstract void newScan(String name);

    public abstract void addPage(BufferedImage image) throws IOException;

    public abstract void addPage(byte[] data) throws IOException;

    public abstract void addPage(IRequestSender s, String page) throws IOException, InterruptedException;

    public abstract void endScan() throws IOException;

    public abstract void endAll() throws IOException;

    public Path allPath() {
        String filename = title + ".pdf";
        if (out != null) {
            return out.resolve(filename);
        } else {
            return Path.of(filename);
        }
    }

    public Path scanPath() {
        String filename = name + ".pdf";
        if (out != null) {
            return out.resolve(filename);
        } else {
            return Path.of(filename);
        }
    }

    private static final class Simulate extends AbstractScanWriter {

        public Simulate(String title, boolean concatenateAll, Path out) {
            super(title, concatenateAll, out);
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
