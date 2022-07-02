package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.scan.iterators.BufferedImagePageIterator;
import fr.poulpogaz.animescandl.scan.iterators.InputStreamPageIterator;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractSimpleScanWebsite extends AbstractWebsite implements ScanWebsite {

    @Override
    public Class<?>[] supportedIterators() {
        return new Class<?>[] {String.class, InputStream.class, BufferedImage.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> PageIterator<P> getPageIterator(Chapter chapter, Class<P> out)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        if (out == String.class) {
            PageIterator<String> iterator = createStringPageIterator(chapter);

            return (PageIterator<P>) iterator;
        } else if (out == InputStream.class) {
            PageIterator<String> iterator = createStringPageIterator(chapter);

            return (PageIterator<P>) new InputStreamPageIterator(iterator, this);
        } else if (out == BufferedImage.class) {
            PageIterator<String> iterator = createStringPageIterator(chapter);

            return (PageIterator<P>) new BufferedImagePageIterator(
                    new InputStreamPageIterator(
                            iterator, this));
        }

        throw new WebsiteException("Unsupported page iterator");
    }

    protected abstract PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, WebsiteException, JsonException;
}
