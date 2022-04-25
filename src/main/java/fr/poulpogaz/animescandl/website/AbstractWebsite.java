package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.utils.FakeUserAgent;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.HttpResponseDecoded;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class AbstractWebsite implements Website {

    private final Logger LOGGER = LogManager.getLogger(AbstractWebsite.class);
    private final HttpClient CLIENT = createClient();


    public HttpHeaders standardHeaders() {
        return new HttpHeaders()
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("User-Agent", FakeUserAgent.getUserAgent());
    }

    protected HttpClient createClient() {
        return HttpClient.newHttpClient();
    }

    @Override
    public HttpResponseDecoded send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<InputStream> rep = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        LOGGER.debug(request.uri());
        LOGGER.debug(request.headers());
        LOGGER.debug(rep.headers());
        LOGGER.debug("Response code: {}", rep.statusCode());

        return new HttpResponseDecoded(rep);
    }

    protected <E extends Element> Element selectNonNull(E e, String cssSelector) throws DOMException {
        Element e2 = e.selectFirst(cssSelector);

        if (e2 == null) {
            throw new DOMException("Can't find element matching: " + cssSelector);
        }

        return e2;
    }

    protected <E extends Element> Element getElementById(E e, String id) throws DOMException {
        Element e2 = e.getElementById(id);

        if (e2 == null) {
            throw new DOMException("Can't find element with id: " + id);
        }

        return e2;
    }

    protected Element first(Elements elements) throws DOMException {
        Element e = elements.first();

        if (e == null) {
            throw new DOMException("First element is null");
        }

        return e;
    }

    protected Element child(Element element, int n) throws WebsiteException {
        if (n < 0 ) {
            throw new IndexOutOfBoundsException("Negative index");
        } else if (n >= element.childrenSize()) {
            throw new WebsiteException("Index out of bounds: " + n + " >= " + element.childrenSize());
        } else {
            return element.child(n);
        }
    }
}
