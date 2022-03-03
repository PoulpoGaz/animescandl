package fr.poulpogaz.animescandl.utils;

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

    private PDDocument document;

    public ScanWriter(String title, boolean concatenateAll, Path out) {
        super(title, concatenateAll, out);
    }

    @Override
    public void newScan(String name) {
        if (document == null) {
            document = new PDDocument();
        }

        this.name = name;
    }

    @Override
    public void addPage(BufferedImage image) throws IOException {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            image = convertRGB(image);
        }

        PDImageXObject obj = JPEGFactory.createFromImage(document, image, 1);
        PDPage pdPage = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
        PDPageContentStream stream = new PDPageContentStream(document, pdPage);
        stream.drawImage(obj, 0, 0);
        stream.close();

        document.addPage(pdPage);
    }

    @Override
    public void addPage(byte[] data) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));

        addPage(image);
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
        BufferedImage img = ImageIO.read(is);
        is.close();

        addPage(img);
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
}
