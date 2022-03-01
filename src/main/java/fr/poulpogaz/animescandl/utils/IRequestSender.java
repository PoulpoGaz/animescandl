package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;

public interface IRequestSender {

    default HttpRequest.Builder standardRequest(String uri) {
        return standardHeaders().request(uri);
    }

    default HttpHeaders standardHeaders() {
        return new HttpHeaders();
    }

    default JsonElement getJson(String url) throws IOException, InterruptedException, JsonException {
        return JsonTreeReader.read(getInputStream(url));
    }

    default JsonElement getJson(String url, HttpHeaders headers) throws IOException, InterruptedException, JsonException {
        return JsonTreeReader.read(getInputStream(url, headers));
    }

    default JsonElement getJson(HttpRequest request) throws IOException, InterruptedException, JsonException {
        return JsonTreeReader.read(getInputStream(request));
    }


    default Document getDocument(String url) throws IOException, InterruptedException {
        return getDocument(standardRequest(url).build());
    }

    default Document getDocument(String url, HttpHeaders headers) throws IOException, InterruptedException {
        HttpRequest.Builder request = standardRequest(url);
        headers.apply(request);

        return getDocument(request.build());
    }

    default Document getDocument(HttpRequest request) throws IOException, InterruptedException {
        InputStream is = getInputStream(request);
        Document document = Jsoup.parse(is, "UTF-8", request.uri().toString());

        if (Utils.WRITE) {
            URI uri = request.uri();
            String url = uri.toASCIIString();

            Path out = Path.of(url.replace(":", "").replace("/", "") + ".html");

            Files.writeString(out, document.outerHtml());
        }

        return document;
    }


    default InputStream getInputStream(String url) throws IOException, InterruptedException {
        return getInputStream(standardRequest(url).build());
    }

    default InputStream getInputStream(String url, HttpHeaders headers) throws IOException, InterruptedException {
        HttpRequest.Builder request = standardRequest(url);
        headers.apply(request);

        return getInputStream(request.build());
    }

    default InputStream getInputStream(HttpRequest request) throws IOException, InterruptedException {
        HttpResponseDecoded rep = GET(request);

        return rep.body();
    }


    default HttpResponseDecoded GET(String url) throws IOException, InterruptedException {
        return GET(standardRequest(url).build());
    }

    default HttpResponseDecoded GET(String url, HttpHeaders headers) throws IOException, InterruptedException {
        HttpRequest.Builder request = standardRequest(url);
        headers.apply(request);

        return GET(request.build());
    }

    HttpResponseDecoded GET(HttpRequest request) throws IOException, InterruptedException;
}
