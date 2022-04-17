package fr.poulpogaz.animescandl.websiteold;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.modelold.Entry;
import fr.poulpogaz.animescandl.modelold.Title;
import fr.poulpogaz.animescandl.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public abstract class AbstractWebsite<E extends Entry, T extends Title> implements Website<T>, IRequestSender {

    private static final Logger LOGGER = LogManager.getLogger(AbstractWebsite.class);

    protected final CookieManager COOKIES = new CookieManager();
    protected final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(COOKIES)
            .build();

    public AbstractWebsite() {

    }

    @Override
    public boolean accept(String url) {
        return url.startsWith(url());
    }

    @Override
    public void process(String url, Settings settings) throws Throwable {
        List<E> entries = fetchList(url, settings);

        if (entries.isEmpty()) {
            return;
        }

        LOGGER.debug("Entries found: {}", entries);
        if (!preDownload(entries, settings)) {
            LOGGER.info("{} already downloaded", url);
            return;
        }

        for (E entry : entries) {
            if (settings.rangeContains(entry.index())) {
                if (Main.noOverwrites.isPresent()) {
                    Path output = getOutputFile(entry, settings);

                    if (output != null && Files.exists(output)) {
                        LOGGER.info("{} already exists", entry);
                        continue;
                    }
                }

                LOGGER.debug("Processing {}", entry.url());
                processEntry(entry, settings);
            }
        }

        postDownload(entries, settings);
    }

    protected abstract List<E> fetchList(String url, Settings settings) throws Throwable;

    /**
     * @return the path to the output file or null
     *          If null, implementations should take care
     *          of not overwriting file if user wants
     */
    protected abstract Path getOutputFile(E entry, Settings settings);

    protected boolean preDownload(List<E> entries, Settings settings) throws Throwable {
        return true;
    }

    protected abstract void processEntry(E entry, Settings settings) throws Throwable;

    protected void postDownload(List<E> entries, Settings settings) throws Throwable {}


    public HttpHeaders standardHeaders() {
        return new HttpHeaders()
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("User-Agent", FakeUserAgent.getUserAgent());
    }

    public HttpResponseDecoded send(HttpRequest request) throws IOException, InterruptedException {
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

        if (Main.verbose.isNotPresent()) {
            System.out.println();
        }
    }

    protected void printErase(String text) {
        if (Main.verbose.isPresent()) {
            LOGGER.info(text);
        } else {
            LOGGER.debug(text);
            System.out.print("\r" + text);
        }
    }

    protected String getJS(String name) throws IOException {
        String jsPath = "/js/" + name().toLowerCase(Locale.ROOT) + "_" + name;

        InputStream is = AbstractWebsite.class.getResourceAsStream(jsPath);
        byte[] bytes = is.readAllBytes();
        is.close();

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
