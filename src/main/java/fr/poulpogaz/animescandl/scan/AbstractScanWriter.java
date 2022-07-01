package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class AbstractScanWriter {

    private static final ASDLLogger LOGGER = Loggers.getLogger(AbstractScanWriter.class);

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
    protected String chapterName;

    public AbstractScanWriter(String title, boolean concatenateAll, Path out) {
        this.title = title;
        this.concatenateAll = concatenateAll;
        this.out = out;
    }

    public void newScan(ScanWebsite s, Chapter chap) throws IOException, JsonException, WebsiteException, InterruptedException {
        Manga m = chap.getManga();
        String chapName = chap.getName().orElse(m.getTitle() + " - " + chap.getChapterNumber());

        newScan(chapName);

        if (Utils.contains(s.supportedIterators(), String.class)) {
            iter(s.getPageIterator(chap, String.class), (url) -> addPage(s, url));

        } else if (Utils.contains(s.supportedIterators(), InputStream.class)) {
            iter(s.getPageIterator(chap, InputStream.class), this::addPage);

        } else if (Utils.contains(s.supportedIterators(), BufferedImage.class)) {
            iter(s.getPageIterator(chap, BufferedImage.class), this::addPage);
        }

        endScan();
    }

    private <T> void iter(PageIterator<T> iterator, AddPage<T> addPage)
            throws IOException, WebsiteException, InterruptedException {
        String max = iterator.nPages()
                .map(String::valueOf)
                .orElse("??");
        int page = 0;

        try {
            while (iterator.hasNext()) {
                addPage.addPage(iterator.next());
                page++;

                LOGGER.info("\r{}/{} pages", page, max);
            }
        } finally {
            LOGGER.newLine();
        }
    }

    public abstract void newScan(String chapterName);

    public abstract void addPage(BufferedImage image) throws IOException;

    public abstract void addPage(byte[] data) throws IOException;

    public abstract void addPage(InputStream is) throws IOException;

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
        String filename = chapterName + ".pdf";
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
        public void newScan(String chapterName) {

        }

        @Override
        public void addPage(BufferedImage image) {

        }

        @Override
        public void addPage(byte[] data) {

        }

        @Override
        public void addPage(InputStream is) {

        }

        @Override
        public void addPage(IRequestSender s, String page) {

        }

        @Override
        public void endScan() {

        }

        @Override
        public void endAll() {

        }
    }

    @FunctionalInterface
    private interface AddPage<T> {
        void addPage(T page) throws IOException, InterruptedException;
    }
}
