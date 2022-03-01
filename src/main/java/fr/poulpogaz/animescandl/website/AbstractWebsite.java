package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Entry;
import fr.poulpogaz.animescandl.model.Title;
import fr.poulpogaz.animescandl.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public abstract class AbstractWebsite<E extends Entry, T extends Title> implements Website<T>, IRequestSender {

    private static final Logger LOGGER = LogManager.getLogger(AbstractWebsite.class);

    protected final CookieManager COOKIES = new CookieManager();
    protected final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(COOKIES)
            .build();

    public static boolean NO_DOWNLOAD = false;

    public AbstractWebsite() {

    }

    @Override
    public boolean accept(String url) {
        return url.startsWith(url());
    }

    @Override
    public void process(String url, Settings settings) throws Throwable {
        List<E> entries = fetchList(url, settings);

        if (entries.size() > 0) {
            LOGGER.debug("Entries found: {}", entries);
            preDownload(entries, settings);

            for (E entry : entries) {
                if (settings.rangeContains(entry.index())) {
                    processEntry(entry, settings);
                }
            }

            postDownload(entries, settings);
        }
    }

    protected abstract List<E> fetchList(String url, Settings settings) throws Throwable;

    protected void preDownload(List<E> entries, Settings settings) throws Throwable {}

    protected abstract void processEntry(E entry, Settings settings) throws Throwable;

    protected void postDownload(List<E> entries, Settings settings) throws Throwable {}


    public HttpHeaders standardHeaders() {
        return new HttpHeaders()
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("User-Agent", FakeUserAgent.getUserAgent());
    }

    public HttpResponseDecoded GET(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<InputStream> rep = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        LOGGER.debug(request.uri());
        LOGGER.debug(request.headers());
        LOGGER.debug(rep.headers());
        LOGGER.debug("Response code: {}", rep.statusCode());

        return new HttpResponseDecoded(rep);
    }

    @Override
    public void dispose() {

    }

    protected void downloadPDF(AbstractScanWriter sw, List<String> pages) {
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            printErase("Page %d/%d".formatted(i + 1, pages.size()));
            try {
                sw.addPage(this, page);
            } catch (IOException | InterruptedException e) {
                LOGGER.warn("Failed to download page {}\nPDF will still be written", page, e);
                return;
            }
        }

        if (!Utils.VERBOSE) {
            System.out.println();
        }
    }

    protected void printErase(String text) {
        if (Utils.VERBOSE) {
            LOGGER.info(text);
        } else {
            LOGGER.debug(text);
            System.out.print("\r" + text);
        }
    }
}
