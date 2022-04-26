package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class ScanWriter extends AbstractScanWriter {

    private static final ASDLLogger LOGGER = Loggers.getLogger(ScanWriter.class);

    private PDDocument document;

    public ScanWriter(String title, boolean concatenateAll, Path out) {
        super(title, concatenateAll, out);
    }

    @Override
    public <C extends Chapter> void newScan(ScanWebsite<?, C> s, C chap) throws IOException, JsonException, WebsiteException, InterruptedException {
        Manga m = chap.getManga();
        String chapName = chap.getName().orElse(m.getTitle() + " - " + chap.getChapterNumber());

        LOGGER.infoln("Writing {}", chapName);
        newScan(chapName);

        try {
            if (Utils.contains(s.supportedIterators(), String.class)) {
                iter(s.getPageIterator(chap, String.class), (url) -> addPage(s, url));

            } else if (Utils.contains(s.supportedIterators(), InputStream.class)) {
                iter(s.getPageIterator(chap, InputStream.class), this::addPage);

            } else if (Utils.contains(s.supportedIterators(), BufferedImage.class)) {
                iter(s.getPageIterator(chap, BufferedImage.class), this::addPage);

            }
        } finally {
            LOGGER.newLine();
        }

        endScan();
    }

    private <T> void iter(PageIterator<T> iterator, AddPage<T> addPage)
            throws IOException, WebsiteException, InterruptedException {
        String max = iterator.nPages()
                .map(String::valueOf)
                .orElse("??");
        int page = 0;

        while (iterator.hasNext()) {
            addPage.addPage(iterator.next());
            page++;

            LOGGER.info("\r{}/{} pages", page, max);
        }
    }

    @Override
    public void newScan(String chapterName) {
        if (document == null) {
            document = new PDDocument();
        }

        this.chapterName = chapterName;
    }

    protected void addPage(PDImageXObject object) throws IOException {
        PDPage pdPage = new PDPage(new PDRectangle(object.getWidth(), object.getHeight()));
        PDPageContentStream stream = new PDPageContentStream(document, pdPage);
        stream.drawImage(object, 0, 0);
        stream.close();

        document.addPage(pdPage);
    }

    @Override
    public void addPage(BufferedImage image) throws IOException {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            image = convertRGB(image);
        }

        PDImageXObject obj = JPEGFactory.createFromImage(document, image, 1);
        addPage(obj);
    }

    @Override
    public void addPage(byte[] data) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));

        addPage(image);
    }

    @Override
    public void addPage(InputStream is) throws IOException {
        addPage(ImageIO.read(is));
    }

    @Override
    public void addPage(IRequestSender s, String page) throws IOException, InterruptedException {
        HttpResponse<InputStream> rep = s.GET(page);

        if (rep.statusCode() < 200 || rep.statusCode() >= 300) {
            String error = new String(rep.body().readAllBytes());
            rep.body().close();

            throw new IOException("Can't add page. Error: " + error);
        }

        InputStream is = rep.body();
        if (page.endsWith("jpg") || page.equals("jpeg")) {
            PDImageXObject xObject = JPEGFactory.createFromStream(document, is);
            is.close();
            addPage(xObject);

        } else {
            BufferedImage img = ImageIO.read(is);
            is.close();

            addPage(img);
        }
    }

    private BufferedImage convertRGB(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = copy.createGraphics();
        try {
            g2d.drawImage(image, 0, 0, null);
        } finally {
            g2d.dispose();
        }

        return copy;
    }

    @Override
    public void endScan() throws IOException {
        if (!concatenateAll) {
            document.save(scanPath().toFile());
            document.close();
            document = null;
        }
    }

    @Override
    public void endAll() throws IOException {
        if (document != null) {
            document.save(allPath().toFile());
            document.close();
            document = null;
        }
    }

    @FunctionalInterface
    private interface AddPage<T> {
        void addPage(T page) throws IOException, InterruptedException;
    }
}
