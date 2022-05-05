package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.MangaWithChapter;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.CompletionWaiter;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;
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

public class Japanread extends AbstractSimpleScanWebsite<MangaWithChapter, Chapter> implements SearchWebsite<MangaWithChapter> {

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
    public HttpRequest.Builder standardRequest(String uri) {
        return super.standardRequest(uri).version(HttpClient.Version.HTTP_1_1);
    }

    @Override
    public MangaWithChapter getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        String mangaURL = getMangaURL(url);
        Document document = getDocument(mangaURL);

        MangaWithChapter.Builder builder = new MangaWithChapter.Builder();
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
                case "Type - Catégories :" -> {
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

        Elements chapters = document.select("a[data-type=chapter]");

        if (chapters.size() > 0) {
            loadChapters(builder, url() + chapters.get(0).attr("href"));
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

    private void parseCategories(Element element, MangaWithChapter.Builder builder) {
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

    private void parseStatus(Element element, MangaWithChapter.Builder builder) throws WebsiteException {
        Element e = selectNonNull(element, ".col-xl-10");
        String text = e.html();

        Status s = switch (text) {
            case "Terminé" -> Status.COMPLETED;
            case "En cours" -> Status.ONGOING;
            default -> null;
        };

        builder.setStatus(s);
    }

    private void parseDescription(Element element, MangaWithChapter.Builder builder) throws WebsiteException {
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

    private void loadChapters(MangaWithChapter.Builder builder, String chapterURL)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        JsonObject manga = getMangaJson(chapterURL);
        JsonObject chaptersObj = manga.getAsObject("chapter");

        for (Map.Entry<String, JsonElement> e : chaptersObj.entrySet()) {
            Chapter.Builder chapBuilder = new Chapter.Builder();
            String id = e.getKey();
            String url = "https://www.japanread.cc/api/?id=%s&type=chapter".formatted(id);
            chapBuilder.setUrl(url);

            JsonObject value = (JsonObject) e.getValue();
            String chapter = value.getAsJsonString("chapter").getAsString();

            chapBuilder.setChapterNumber(Utils.getFirstInt(chapter));
            chapBuilder.setName(chapter);

            builder.addChapter(chapBuilder);
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
    public List<Chapter> getChapters(MangaWithChapter manga)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        return manga.getChapters();
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, JsonException, WebsiteException {
        return new StringPageIterator(chapter);
    }

    private class StringPageIterator implements PageIterator<String> {

        private static final CefCookieManager manager = CefCookieManager.getGlobalManager();

        private final String baseURL;
        private final JsonArray pages;
        private int index;

        public StringPageIterator(Chapter chapter)
                throws JsonException, IOException, InterruptedException, WebsiteException {
            HttpHeaders headers = standardHeaders()
                    .header("a", "aa37ec6b0977")
                    .header("referer", chapter.getManga().getUrl())
                    .header("x-requested-with", "XMLHttpRequest");

            setCookies(headers);

            JsonObject object = (JsonObject) getJson(chapter.getUrl(), headers);
            baseURL = url() + object.getAsString("baseImagesUrl") + "/";
            pages = object.getAsArray("page_array");
        }

        private void setCookies(HttpHeaders headers) throws WebsiteException, InterruptedException {
            CompletionWaiter<Void> waiter = new CompletionWaiter<>();

            manager.visitUrlCookies("https://www.japanread.cc/", true,
                (cookie, count, total, delete) -> {
                    if ("PHPSESSID".equals(cookie.name)) {
                        headers.header("cookie", cookie.name + "=" + cookie.value);
                        waiter.complete();
                        return false;
                    }

                    if (count + 1 == total) {
                        waiter.complete();
                    }

                    return true;
                });

            waiter.waitUntilCompletion(-1);
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
    public FilterList getSearchFilter() {
        return null;
    }

    @Override
    public List<MangaWithChapter> search(String search, FilterList filter) {
        return null;
    }
}
