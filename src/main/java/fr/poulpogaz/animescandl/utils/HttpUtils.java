package fr.poulpogaz.animescandl.utils;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.validator.routines.UrlValidator;
import org.openqa.selenium.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class HttpUtils {

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
}
