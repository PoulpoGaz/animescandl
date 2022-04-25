package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.CompletionWaiter;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public abstract class BaseIterator<T> implements PageIterator<T> {

    protected final Japscan japscan;
    protected final Chapter chapter;

    /**
     * In the left, the url to the webpage
     * In the right, the encrypted url to the image
     */
    protected List<Pair<String, String>> pages;
    protected int index = 0;

    protected final Map<Character, Character> map = new HashMap<>();

    public BaseIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException, WebsiteException {
        this.japscan = japscan;
        this.chapter = chapter;
        loadPages();
    }

    @Override
    public boolean hasNext() {
        return index < pages.size();
    }

    protected String nextURL() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Pair<String, String> pair = pages.get(index);
        return decrypt(pair.right());
    }

    protected String decrypt(String encrypted) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://cdn.statically.io/img/c.japscan.ws/");

        for (int i = 21; i < encrypted.length() - 4; i++) {
            if (encrypted.charAt(i) != '/') {
                builder.append(map.get(encrypted.charAt(i)));
            } else {
                builder.append('/');
            }
        }
        int extensionStart = encrypted.lastIndexOf('.');

        builder.append(encrypted, extensionStart, encrypted.length());

        return builder.toString();
    }

    protected void loadPages() throws IOException, InterruptedException, WebsiteException {
        Document document = japscan.getDocument(chapter.getUrl());
        Elements options = document.select("#pages > option");

        pages = options.stream()
                .map((e) -> {
                    String w = japscan.url() + e.attr("value");
                    String encrypted = e.attr("data-img");

                    return new Pair<>(w, encrypted);
                })
                .toList();

        String w = pages.get(0).left();
        String e = pages.get(0).right();
        fillMap(w, e);
    }

    protected void fillMap(String webpage, String encrypted) throws WebsiteException, InterruptedException {
        map.clear();

        String decrypted = getDecrypted(webpage);

        int end = encrypted.lastIndexOf('.');

        int dI = 43; // decryptedI
        int eI = 21; // encryptedI
        for (; dI < end; dI++, eI++) {
            if (decrypted.charAt(dI) != '/') {
                map.put(encrypted.charAt(eI), decrypted.charAt(dI));
            }
        }
    }

    // The difficult function
    protected String getDecrypted(String webpage) throws WebsiteException, InterruptedException {
        CompletionWaiter<String> waiter = new CompletionWaiter<>();
        CEFHelper helper = CEFHelper.getInstance();

        helper.loadURL(webpage);
        helper.getClient().addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser,
                                                                       CefFrame frame,
                                                                       CefRequest request,
                                                                       boolean isNavigation,
                                                                       boolean isDownload,
                                                                       String requestInitiator,
                                                                       BoolRef disableDefaultHandling) {
                String url = request.getURL();

                if (url.contains("https://cdn.statically.io/img/c.japscan.ws/")) {
                    waiter.complete(url);
                }

                return null;
            }
        });
        helper.setVisible(true);

        return waiter.waitUntilCompletion(10000);
    }

    @Override
    public Optional<Integer> nPages() {
        return Optional.of(pages.size());
    }
}
