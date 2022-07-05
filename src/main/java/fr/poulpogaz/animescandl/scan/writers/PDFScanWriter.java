package fr.poulpogaz.animescandl.scan.writers;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PDFScanWriter implements IScanWriter {

    private static final ASDLLogger LOGGER = Loggers.getLogger(PDFScanWriter.class);

    private final Manga manga;
    private final boolean continueDownload;

    private PDDocument currentDocument;

    public PDFScanWriter(Manga manga, boolean continueDownload) {
        this.manga = manga;
        this.continueDownload = continueDownload;
    }

    @Override
    public void writeScans(ScanWebsite website, List<Chapter> chapters, Path out)
            throws IOException, JsonException, WebsiteException, InterruptedException {
        Path partFile = Path.of(out.toString() + ".part");

        try (PDDocument document = getDocument(partFile)) {
            currentDocument = document;

            boolean downloadComplete = false;

            try {
                int page = 0;

                for (Chapter chapter : chapters) {
                    if (Utils.contains(website.supportedIterators(), String.class)) {
                        page = iter(website.getPageIterator(chapter, String.class), (url) -> addPage(website, url), page);

                    } else if (Utils.contains(website.supportedIterators(), InputStream.class)) {
                        page = iter(website.getPageIterator(chapter, InputStream.class), this::addPage, page);

                    } else if (Utils.contains(website.supportedIterators(), BufferedImage.class)) {
                        page = iter(website.getPageIterator(chapter, BufferedImage.class), this::addPage, page);
                    }
                }
                downloadComplete = true;
            } finally {
                if (downloadComplete) {
                    writeDocument(out);
                } else if (document.getNumberOfPages() > 0) {
                    writeDocumentSilently(partFile);
                }
            }

        }
    }

    /**
     * Iter over all page of a PageIterator and add them. It skips already downloaded pages
     *
     * @param iterator the iterator
     * @param addPage the object that will add the page
     * @param nPage the number of page processed by all iter calls.
     *              This is not the same number as {@code currentDocument.getNumberOfPages()}
     * @return nPage + the number of page of the page iterator
     */
    private <T> int iter(PageIterator<T> iterator, AddPage<T> addPage, int nPage)
            throws IOException, WebsiteException, InterruptedException {
        int n = iterator.nPages().orElse(-1);

        // skip the whole chapter
        if (n >= 0 && nPage + n < currentDocument.getNumberOfPages()) {
            LOGGER.debugln("SKIPPING WHOLE CHAPTER");
            return nPage + n;
        }

        String max = iterator.nPages().map(String::valueOf).orElse("??");
        int page = 0;

        try {
            while (iterator.hasNext()) {
                page++;
                T next = iterator.next();

                if (nPage + page > currentDocument.getNumberOfPages()) {
                    addPage.addPage(next);

                    LOGGER.info("\r{}/{} pages", page, max);
                }
            }

            return nPage + page;
        } finally {
            LOGGER.newLine();
        }
    }


    private PDDocument getDocument(Path partFile) throws IOException {
        if (continueDownload && Files.exists(partFile)) {
            PDDocument document = PDDocument.load(partFile.toFile());
            Utils.deleteSilently(partFile);

            LOGGER.debugln("Continuing download. npage = {}", document.getNumberOfPages());

            return document;
        } else {
            return new PDDocument();
        }
    }

    private void writeDocument(Path out) throws IOException {
        currentDocument.save(out.toFile());
    }

    private void writeDocumentSilently(Path out) {
        try {
            currentDocument.save(out.toFile());
        } catch (IOException e) {
            LOGGER.warnln("Failed to write pdf to {}", out, e);
        }
    }


    private void addPage(BufferedImage image) throws IOException {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            image = convertRGB(image);
        }

        PDImageXObject obj = JPEGFactory.createFromImage(currentDocument, image, 1);
        addPage(obj);
    }

    private void addPage(InputStream is) throws IOException {
        addPage(ImageIO.read(is));
        is.close();
    }

    private void addPage(IRequestSender s, String page) throws IOException, InterruptedException {
        HttpResponse<InputStream> rep = s.GET(page);

        if (rep.statusCode() < 200 || rep.statusCode() >= 300) {

            try (InputStream body = rep.body()) {
                String error = new String(body.readAllBytes());

                throw new IOException("Can't add page. Error: " + error);
            }
        }

        InputStream is = rep.body();
        if (page.endsWith("jpg") || page.equals("jpeg")) {
            PDImageXObject xObject = JPEGFactory.createFromStream(currentDocument, is);
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


    private void addPage(PDImageXObject object) throws IOException {
        PDPage pdPage = new PDPage(new PDRectangle(object.getWidth(), object.getHeight()));
        PDPageContentStream stream = new PDPageContentStream(currentDocument, pdPage);
        stream.drawImage(object, 0, 0);
        stream.close();

        currentDocument.addPage(pdPage);
    }
}
