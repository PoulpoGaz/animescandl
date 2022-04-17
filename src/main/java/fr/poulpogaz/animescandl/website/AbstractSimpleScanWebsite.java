package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.website.iterators.BufferedImagePageIterator;
import fr.poulpogaz.animescandl.website.iterators.InputStreamPageIterator;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractSimpleScanWebsite<M extends Manga, C extends Chapter>
        extends AbstractScanWebsite<M, C>{

    @Override
    public Class<?>[] supportedIterators() {
        return new Class<?>[] {String.class, InputStream.class, BufferedImage.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> PageIterator<P> getPageIterator(C chapter, Class<P> out)
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
