package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonElement;
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class HttpUtils {

    private static final Logger LOGGER = LogManager.getLogger(HttpUtils.class);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
    private static final IRequestSender STANDARD = createRequestSender();

    public static InputStream decodeInputStream(HttpResponse<InputStream> rep) throws IOException {
        String encoding = rep.headers().firstValue("content-encoding").orElse(null);
        InputStream is = rep.body();

        if (encoding == null) {
            return is;
        } else if (encoding.equals("gzip")) {
            return new GZIPInputStream(is);
        } else if (encoding.equals("br")) {
            return new BrotliCompressorInputStream(is);
        } else if (encoding.equals("deflate")) {
            return new DeflateCompressorInputStream(is);
        } else {
            return null;
        }
    }

    public static String toString(CookieStore cookieStore) {
        return toString(cookieStore.getCookies());
    }

    public static String toString(CookieStore cookieStore, URI uri) {
        return toString(cookieStore.get(uri));
    }

    public static String toString(Collection<HttpCookie> cookies) {
        StringBuilder builder = new StringBuilder();

        Iterator<HttpCookie> iterator = cookies.iterator();
        for (int i = 0; i < cookies.size(); i++) {
            builder.append(iterator.next());

            if (i + 1 < cookies.size()) {
                builder.append(';');
            }
        }

        return builder.toString();
    }

    public static String stoString(Collection<Cookie> cookies) {
        StringBuilder builder = new StringBuilder();

        Iterator<Cookie> iterator = cookies.iterator();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie c = iterator.next();
            builder.append(c.getName()).append("=").append(c.getValue());

            if (i + 1 < cookies.size()) {
                builder.append(';');
            }
        }

        return builder.toString();
    }

    public static boolean isValidURL(String url) {
        return UrlValidator.getInstance().isValid(url);
    }


    public static JsonElement getJson(String url) throws JsonException, IOException, InterruptedException {
        return STANDARD.getJson(url);
    }

    public static JsonElement getJson(String url, HttpHeaders headers) throws JsonException, IOException, InterruptedException {
        return STANDARD.getJson(url, headers);
    }

    public static JsonElement getJson(HttpRequest request) throws JsonException, IOException, InterruptedException {
        return STANDARD.getJson(request);
    }

    public static Document getDocument(String url) throws IOException, InterruptedException {
        return STANDARD.getDocument(url);
    }

    public static Document getDocument(String url, HttpHeaders headers) throws IOException, InterruptedException {
        return STANDARD.getDocument(url, headers);
    }

    public static Document getDocument(HttpRequest request) throws IOException, InterruptedException {
        return STANDARD.getDocument(request);
    }

    public static InputStream getInputStream(String url) throws IOException, InterruptedException {
        return STANDARD.getInputStream(url);
    }

    public static InputStream getInputStream(String url, HttpHeaders headers) throws IOException, InterruptedException {
        return STANDARD.getInputStream(url, headers);
    }

    public static InputStream getInputStream(HttpRequest request) throws IOException, InterruptedException {
        return STANDARD.getInputStream(request);
    }

    public static HttpResponseDecoded GET(String url) throws IOException, InterruptedException {
        return STANDARD.GET(url);
    }

    public static HttpResponseDecoded GET(String url, HttpHeaders headers) throws IOException, InterruptedException {
        return STANDARD.GET(url, headers);
    }

    public static HttpResponseDecoded GET(HttpRequest request) throws IOException, InterruptedException {
        return STANDARD.GET(request);
    }


    private static IRequestSender createRequestSender() {
        return request -> {
            HttpResponse<InputStream> rep = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            LOGGER.debug(request.uri());
            LOGGER.debug(request.headers());
            LOGGER.debug(rep.headers());
            LOGGER.debug("Response code: {}", rep.statusCode());

            return new HttpResponseDecoded(rep);
        };
    }
}
