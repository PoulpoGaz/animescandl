package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.*;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.DOMException;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.TriStateCheckBox;
import fr.poulpogaz.animescandl.website.filter.url.ListTriStateCheckBox;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilter;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilterList;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Japanread
        extends AbstractSimpleScanWebsite<Manga, Chapter>
        implements SearchWebsite<Manga> {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Japanread.class);

    private static final String MANGA_INDICATOR = "[MANGA]";
    private static final String CHAP_INDICATOR = "[CHAPTER]";

    private final String MANGA_CHAPTER_EXTRACTOR;

    public Japanread() throws IOException {
        String path = "/js/japanread_manga_chapter_extractor.js";

        InputStream is = Japanread.class.getResourceAsStream(path);
        Objects.requireNonNull(is);
        byte[] bytes = is.readAllBytes();
        is.close();

        MANGA_CHAPTER_EXTRACTOR =
                new String(bytes, StandardCharsets.UTF_8)
                        .formatted(MANGA_INDICATOR, CHAP_INDICATOR);
    }

    @Override
    public String name() {
        return "japanread";
    }

    @Override
    public String version() {
        return "dev2";
    }

    @Override
    public String url() {
        return "https://www.japanread.cc";
    }

    @Override
    public boolean needCEF() {
        return true;
    }

    @Override
    public boolean isChapterURL(String url) {
        Pattern pattern = Pattern.compile("^https://www\\.japanread\\.cc/manga/[^/]*/\\d*$");

        return pattern.matcher(url).find();
    }

    @Override
    public boolean isMangaURL(String url) {
        Pattern pattern = Pattern.compile("^https://www\\.japanread\\.cc/manga/[^/]*/?$");

        return pattern.matcher(url).find();
    }

    @Override
    public HttpHeaders standardHeaders() {
        return super.standardHeaders();
    }

    @Override
    public Manga getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        String mangaURL = getMangaURL(url);
        Document document = getDocument(mangaURL);

        Manga.Builder builder = new Manga.Builder();
        builder.setUrl(mangaURL);

        Element title = selectNonNull(document, ".text-white");
        builder.setTitle(Utils.getFirstNonEmptyText(title, true));

        Element thumbnail = selectNonNull(document, "img[alt^=couverture]");
        builder.setThumbnailURL(thumbnail.attr("src"));

        Elements elements = document.select(".col-xl-9 > *");

        for (Element element : elements) {
            String t = selectNonNull(element, "div .font-weight-bold").html();

            switch (t) {
                case "Auteur(s) :" -> {
                    String author = selectNonNull(element, "a").html();

                    builder.setAuthor(author);
                }
                case "Artiste(s) :" -> {
                    String artist = selectNonNull(element, "a").html();

                    builder.setArtist(artist);
                }
                case "Type - Catégories :", "Catégories :" -> {
                    parseCategories(element, builder);
                }
                case "Statut :" -> {
                    parseStatus(element, builder);
                }
                case "Note :" -> {
                    String score = selectNonNull(element, ".js_avg").html();
                    builder.setScore(Float.parseFloat(score));
                }
                case "Description :" -> {
                    parseDescription(element, builder);
                }
            }
        }

        return builder.build();
    }

    private String getMangaURL(String url) throws UnsupportedURLException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            return Utils.getRegexGroup(url, "^(https://www\\.japanread\\.cc/manga/[^/]*)/\\d*$");
        } else {
            throw new UnsupportedURLException(this, url);
        }
    }

    private void parseCategories(Element element, Manga.Builder builder) {
        Elements elements = element.select("a");

        for (Element a : elements) {
            String attr = a.attr("style");
            if (attr.equals("display:none")) {
                continue;
            }

            Element e = a.selectFirst("span");

            builder.addGenre(Objects.requireNonNullElse(e, a).html());
        }
    }

    private void parseStatus(Element element, Manga.Builder builder) throws WebsiteException {
        Element e = selectNonNull(element, ".col-xl-10");
        String text = e.html();

        Status s = switch (text) {
            case "Terminé" -> Status.COMPLETED;
            case "En cours" -> Status.ONGOING;
            default -> null;
        };

        builder.setStatus(s);
    }

    private void parseDescription(Element element, Manga.Builder builder) throws WebsiteException {
        Element description = selectNonNull(element, ".col-lg-9");

        StringBuilder b = new StringBuilder();
        for (Node n : description.childNodes()) {
            if (n instanceof TextNode t) {
                b.append(t.text());
            } else if (n instanceof Element e && e.is("br")) {
                b.append("\n");
            }
        }

        builder.setDescription(b.toString());
    }

    @Override
    public List<Chapter> getChapters(Manga manga)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        Document document = getDocument(manga.getUrl());
        Elements chapterElements = document.select("a[data-type=chapter]");

        List<Chapter> chapters = new ArrayList<>();
        if (chapterElements.size() > 0) {
            loadChapters(chapters, manga, url() + chapterElements.get(0).attr("href"));
        }

        return chapters;
    }

    private void loadChapters(List<Chapter> chapters, Manga manga, String chapterURL)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        JsonObject json = getMangaJson(chapterURL);
        JsonObject chaptersObj = json.getAsObject("chapter");

        for (Map.Entry<String, JsonElement> e : chaptersObj.entrySet()) {
            Chapter.Builder chapBuilder = new Chapter.Builder();
            String id = e.getKey();
            String url = "https://www.japanread.cc/api/?id=%s&type=chapter".formatted(id);
            chapBuilder.setUrl(url);

            JsonObject value = (JsonObject) e.getValue();
            String chapter = value.getAsJsonString("chapter").getAsString();

            chapBuilder.setChapterNumber(Utils.getFirstInt(chapter));
            chapBuilder.setName(chapter);
            chapBuilder.setManga(manga);

            chapters.add(chapBuilder.build());
        }
    }

    /* TODO: https://bitbucket.org/chromiumembedded/java-cef/pull-requests/81
        Will be done in a future jcef release I think
     */
    protected JsonObject getMangaJson(String chapterURL)
            throws WebsiteException, JsonException, IOException, InterruptedException {
        String rawJson = getRawJson(chapterURL);

        String json = rawJson.substring(MANGA_INDICATOR.length() );
        json = removeBackslash(json);
        StringReader sr = new StringReader(json);

        return (JsonObject) JsonTreeReader.read(sr);
    }

    protected String getRawJson(String chapterURL) throws WebsiteException, InterruptedException {
        CompletionWaiter<String> waiter = new CompletionWaiter<>();

        CEFHelper helper = CEFHelper.getInstance();
        helper.loadURL(chapterURL);

        CefClient client = helper.getClient();
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                if (!waiter.isCompleted()) {
                    browser.executeJavaScript(MANGA_CHAPTER_EXTRACTOR, "my_code.js", 0);
                }
            }
        });

        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {
                if (message.startsWith(MANGA_INDICATOR)) {
                    waiter.complete(message);
                }

                return false;
            }
        });
        helper.setVisible(true);

        return waiter.waitUntilCompletion(10000);
    }

    protected String removeBackslash(String text) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\\') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\\') {
                    builder.append('\\');
                    i++;
                }
            } else {
                builder.append(text.charAt(i));
            }
        }

        return builder.toString();
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, JsonException, WebsiteException {
        return new StringPageIterator(chapter);
    }

    private class StringPageIterator implements PageIterator<String> {

        private final String baseURL;
        private final JsonArray pages;
        private int index;

        public StringPageIterator(Chapter chapter)
                throws JsonException, IOException, InterruptedException, WebsiteException {
            HttpHeaders headers = standardHeaders()
                    .header("a", "aa37ec6b0977")
                    .header("referer", chapter.getManga().getUrl())
                    .header("x-requested-with", "XMLHttpRequest");

            CefCookie cookie = CEFHelper.getCookie("https://www.japanread.cc/", "PHPSESSID");
            if (cookie != null) {
                headers.header("cookie", cookie.name + "=" + cookie.value);
            }

            JsonObject object = (JsonObject) getJson(chapter.getUrl(), headers);
            baseURL = url() + object.getAsString("baseImagesUrl") + "/";
            pages = object.getAsArray("page_array");
        }

        @Override
        public boolean hasNext() {
            return index < pages.size();
        }

        @Override
        public String next() throws WebsiteException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Optional<String> url = pages.getOptionalString(index);
            index++;

            String u = url.orElseThrow(WebsiteException::new);

            return baseURL + URLEncoder.encode(u, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // java replace ' ' with '+' but I need "%20"
        }

        @Override
        public Optional<Integer> nPages() {
            return Optional.of(pages.size());
        }
    }

    @Override
    public Document getDocument(String url) throws IOException, InterruptedException {
        try {
            return getDocument(url, standardHeaders(), true);
        } catch (WebsiteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document getDocument(String url, HttpHeaders headers) throws IOException, InterruptedException {
        try {
            return getDocument(url, headers, false);
        } catch (WebsiteException e) {
            throw new RuntimeException(e);
        }
    }

    protected Document getDocument(String url, HttpHeaders headers, boolean standardHeaders)
            throws WebsiteException, IOException, InterruptedException {
        if (standardHeaders) {
            Document cached = getCachedDocument(url);

            if (cached != null) {
                return cached;
            }
        }

        Map<String, CefCookie> cookies = CEFHelper.getCookies(url(), "cf_.*");

        if (cookies.size() == 0) {
            bypassCloudflare(url);
            cookies = CEFHelper.getCookies(url(), "cf_.*");
        }

        for (CefCookie cookie : cookies.values()) {
            headers.header("cookie", cookie.name + "=" + cookie.value);
        }

        Document document = super.getDocument(url, headers);

        if (standardHeaders && document != null) {
            DOCUMENT_CACHE.put(url, document);
        }

        return document;
    }

    protected void bypassCloudflare(String url) throws WebsiteException, InterruptedException {
        CompletionWaiter<Void> waiter = new CompletionWaiter<>();

        CEFHelper helper = CEFHelper.getInstance();
        helper.loadURL(url);

        CefCookieAccessFilter filter = new CefCookieAccessFilterAdapter() {
            @Override
            public boolean canSaveCookie(CefBrowser browser, CefFrame frame, CefRequest request, CefResponse response, CefCookie cookie) {
                LOGGER.debugln("{} = {}", cookie.name, cookie.value);
                if (cookie.name.equals("cf_clearance")) {
                    waiter.complete();
                }

                return true;
            }
        };

        helper.getClient().addRequestHandler(createRequestHandler(filter));
        helper.setVisible(true);
        waiter.waitUntilCompletion(60000);
    }

    protected CefRequestHandlerAdapter createRequestHandler(CefCookieAccessFilter cookieAccessFilter) {
        CefResourceRequestHandler resourceHandler = new CefResourceRequestHandlerAdapter() {
            @Override
            public CefCookieAccessFilter getCookieAccessFilter(CefBrowser browser, CefFrame frame, CefRequest request) {
                return cookieAccessFilter;
            }
        };

        return new CefRequestHandlerAdapter() {
            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
                return resourceHandler;
            }
        };
    }

    // withCategories=1;27&withoutCategories=21;7&withTypes=5&withoutTypes=6&q=c&author_name=a&released_year=b&status=1
    @Override
    public FilterList getSearchFilter() {
        return new UrlFilterList.Builder()
                .text("Auteur / Artiste", "author_name")
                .text("Année de sortie", "released_year")
                .select("Status", "status")
                    .addVal("Sélectionner un statut")
                    .add("En cours", "1")
                    .add("Terminé", "2")
                    .build()
                .group("Type")
                    .triStateCheckBox("Novel", "withTypes", "5", "withoutTypes", "5")
                    .triStateCheckBox("Doujinshi", "withTypes", "6", "withoutTypes", "6")
                    .build()
                .group("Catégories")
                .addFilter(newTriStateCheckBox("4-koma", "43"))
                .addFilter(newTriStateCheckBox("Action", "1"))
                .addFilter(newTriStateCheckBox("Adulte", "27"))
                .addFilter(newTriStateCheckBox("Amitié", "20"))
                .addFilter(newTriStateCheckBox("Amour", "21"))
                .addFilter(newTriStateCheckBox("Arts martiaux", "7"))
                .addFilter(newTriStateCheckBox("Aventure", "3"))
                .addFilter(newTriStateCheckBox("Boys Love", "44"))
                .addFilter(newTriStateCheckBox("Combat", "6"))
                .addFilter(newTriStateCheckBox("Comédie", "5"))
                .addFilter(newTriStateCheckBox("Drame", "4"))
                .addFilter(newTriStateCheckBox("Ecchi", "12"))
                .addFilter(newTriStateCheckBox("Fantastique", "16"))
                .addFilter(newTriStateCheckBox("Gender Bender", "29"))
                .addFilter(newTriStateCheckBox("Guerre", "8"))
                .addFilter(newTriStateCheckBox("Harem", "22"))
                .addFilter(newTriStateCheckBox("Hentai", "23"))
                .addFilter(newTriStateCheckBox("Historique", "15"))
                .addFilter(newTriStateCheckBox("Horreur", "19"))
                .addFilter(newTriStateCheckBox("Josei", "13"))
                .addFilter(newTriStateCheckBox("Mature", "30"))
                .addFilter(newTriStateCheckBox("Mecha", "18"))
                .addFilter(newTriStateCheckBox("Mystère", "31"))
                .addFilter(newTriStateCheckBox("One Shot", "32"))
                .addFilter(newTriStateCheckBox("Parodie", "42"))
                .addFilter(newTriStateCheckBox("Policier", "17"))
                .addFilter(newTriStateCheckBox("Psychologique", "33"))
                .addFilter(newTriStateCheckBox("Romance", "9"))
                .addFilter(newTriStateCheckBox("Science-fiction", "25"))
                .addFilter(newTriStateCheckBox("Seinen", "11"))
                .addFilter(newTriStateCheckBox("Shojo", "10"))
                .addFilter(newTriStateCheckBox("Shôjo Ai", "26"))
                .addFilter(newTriStateCheckBox("Shônen", "2"))
                .addFilter(newTriStateCheckBox("Shônen Ai", "35"))
                .addFilter(newTriStateCheckBox("Smut", "37"))
                .addFilter(newTriStateCheckBox("Sports", "14"))
                .addFilter(newTriStateCheckBox("Surnaturel", "38"))
                .addFilter(newTriStateCheckBox("Tragédie", "39"))
                .addFilter(newTriStateCheckBox("Tranches de vie", "36"))
                .addFilter(newTriStateCheckBox("Vie scolaire", "34"))
                .addFilter(newTriStateCheckBox("Webtoons", "40"))
                .addFilter(newTriStateCheckBox("Yaoi", "24"))
                .addFilter(newTriStateCheckBox("Yuri", "41"))
                .build()
                .build();
    }

    private ListTriStateCheckBox newTriStateCheckBox(String name, String id) {
        return new ListTriStateCheckBox(name, "withCategories", "withoutCategories", id);
    }

    @Override
    public List<Manga> search(String search, FilterList filter) throws IOException, WebsiteException, InterruptedException {
        if (filter instanceof UrlFilterList urlFilterList) {
            List<Manga> mangas = new ArrayList<>();

            int offset = filter.getOffset() % 16;
            int page = 1 + filter.getOffset() / 16;
            while (mangas.size() < filter.getLimit() &&
                    search(page, offset, search, urlFilterList, mangas)) {
                page++;
                offset = 0;
            }

            return mangas;
        }

        return List.of();
    }

    private boolean search(int page, int offset, String search, UrlFilterList urlFilterList, List<Manga> out)
            throws IOException, InterruptedException, WebsiteException {
        HttpQueryParameterBuilder b = new HttpQueryParameterBuilder();
        b.setAppender(JAPSCAN_APPENDER);
        b.add("q", Objects.requireNonNullElse(search, ""));
        b.add("page", String.valueOf(page));

        String url = urlFilterList.createRequest(url() + "/manga-list", b);

        Document document = getDocument(url);
        Elements mangas = document.select(".row > .col-12.col-lg-6.border-bottom.my-3");

        int max = Math.min(mangas.size(), urlFilterList.getLimit() - out.size());
        for (int i = offset; i < max; i++) {
            out.add(getMangaFromSearch(mangas.get(i)));
        }

        return !document.select("[rel=next]").isEmpty();
    }

    private Manga getMangaFromSearch(Element e) throws DOMException {
        Manga.Builder builder = new Manga.Builder();

        Element url = selectNonNull(e, "a[title]");
        builder.setUrl(url() + url.attr("href"));
        builder.setTitle(url.text());
        builder.setThumbnailURL(selectNonNull(e, "img").attr("src"));

        Element scoreElement = selectNonNull(e, ".text-primary");
        String score = Utils.getFirstNonEmptyText(scoreElement, true);
        builder.setScore(Utils.getFirstFloat(score));

        builder.setDescription(selectNonNull(e, ".text-muted").text());

        return builder.build();
    }

    private static final HttpQueryParameterBuilder.Appender JAPSCAN_APPENDER = new HttpQueryParameterBuilder.Appender() {

        @Override
        public void before(StringBuilder out, String paramName) {
            out.append(URLEncoder.encode(paramName, StandardCharsets.UTF_8))
                    .append("=");
        }

        @Override
        public void append(StringBuilder out, String paramName, String arg, int index, int size) {
            out.append(URLEncoder.encode(arg, StandardCharsets.UTF_8));

            if (index + 1 < size) {
                out.append(";");
            }
        }
    };
}
