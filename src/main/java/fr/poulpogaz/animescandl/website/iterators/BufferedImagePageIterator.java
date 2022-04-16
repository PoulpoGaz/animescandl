package fr.poulpogaz.animescandl.website.iterators;

import fr.poulpogaz.animescandl.website.WebsiteException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public class BufferedImagePageIterator implements PageIterator<BufferedImage> {

    private final PageIterator<InputStream> isIterator;

    public BufferedImagePageIterator(PageIterator<InputStream> isIterator) {
        this.isIterator = Objects.requireNonNull(isIterator);
    }

    @Override
    public boolean hasNext() {
        return isIterator.hasNext();
    }

    @Override
    public BufferedImage next() throws IOException, InterruptedException, WebsiteException {
        InputStream next = isIterator.next();
        BufferedImage img = ImageIO.read(next);
        next.close();

        return img;
    }

    @Override
    public Optional<Integer> nPages() {
        return isIterator.nPages();
    }
}
