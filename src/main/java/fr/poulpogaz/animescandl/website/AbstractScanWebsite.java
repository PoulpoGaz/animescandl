package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.utils.FakeUserAgent;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.HttpResponseDecoded;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.website.iterators.BufferedImagePageIterator;
import fr.poulpogaz.animescandl.website.iterators.InputStreamPageIterator;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class AbstractScanWebsite<M extends Manga, C extends Chapter>
        implements ScanWebsite<M, C>, IRequestSender {

    private final Logger LOGGER = LogManager.getLogger(AbstractScanWebsite.class);
    private final HttpClient CLIENT = createClient();


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


    public HttpHeaders standardHeaders() {
        return new HttpHeaders()
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("User-Agent", FakeUserAgent.getUserAgent());
    }

    protected HttpClient createClient() {
        return HttpClient.newHttpClient();
    }

    @Override
    public HttpResponseDecoded GET(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<InputStream> rep = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        LOGGER.debug(request.uri());
        LOGGER.debug(request.headers());
        LOGGER.debug(rep.headers());
        LOGGER.debug("Response code: {}", rep.statusCode());

        return new HttpResponseDecoded(rep);
    }
}
