package fr.poulpogaz.animescandl.websiteold;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.modelold.Chapter;
import fr.poulpogaz.animescandl.modelold.DefaultTitle;
import fr.poulpogaz.animescandl.utils.*;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import fr.poulpogaz.json.tree.JsonValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @version 28.02.2022
 */
public class Japanread extends AbstractWebsite<Chapter, DefaultTitle> {

    public static final Japanread INSTANCE = new Japanread();

    private static final Logger LOGGER = LogManager.getLogger(Japanread.class);

    private static final String MANGA_INDICATOR = "[MANGA]";
    private static final String CHAP_INDICATOR = "[CHAPTER]";

    private final String MANGA_CHAPTER_EXTRACTOR;

    private AbstractScanWriter sw;

    private Japanread() {
        try {
            MANGA_CHAPTER_EXTRACTOR = getJS("manga_chapter_extractor.js")
                    .formatted(MANGA_INDICATOR, CHAP_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpRequest.Builder standardRequest(String uri) {
        return super.standardRequest(uri).version(HttpClient.Version.HTTP_1_1);
    }

    @Override
    protected List<Chapter> fetchList(String url, Settings settings) throws Throwable {
        ChromeDriver driver = WebDriver.init(PageLoadStrategy.EAGER);

        String chapterURL;
        if (isChapterURL(url)) {
            chapterURL = url;
        } else if (isChapterListURL(url)) {
            chapterURL = getChapterURL(url);
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }

        driver.get(chapterURL);
        driver.executeScript(MANGA_CHAPTER_EXTRACTOR);

        JsonObject manga = getMangaJson(driver);

        String title = manga.getAsObject("manga").getAsJsonString("title").getAsString();
        JsonObject chaptersObj = manga.getAsObject("chapter");

        List<Chapter> chapters = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : chaptersObj.entrySet()) {
            String id = e.getKey();

            JsonObject value = (JsonObject) e.getValue();
            String chapter = value.getAsJsonString("chapter").getAsString();
            int index = Utils.getFirstInt(chapter);

            chapters.add(new Chapter(id, index, title, null, chapter));
        }

        chapters.sort(Utils::chapterComparator);

        return chapters;
    }

    protected boolean isChapterURL(String url) {
        Pattern pattern = Pattern.compile("https://www\\.japanread\\.cc/manga/[^/]*/\\d");

        return pattern.matcher(url).find();
    }

    protected boolean isChapterListURL(String url) {
        Pattern pattern = Pattern.compile("https://www\\.japanread\\.cc/manga/[^/]*/?$");

        return pattern.matcher(url).find();
    }

    protected String getChapterURL(String listURL) throws IOException, InterruptedException {
        Document document = getDocument(listURL);

        Element e = document.selectFirst("a[data-type]");

        return url() + e.attr("href");
    }

    protected JsonObject getMangaJson(ChromeDriver driver) throws WebsiteException, JsonException, IOException {
        LogEntry log = getFirstMatching(driver, (e) -> e.getMessage().contains("[MANGA]"), 7500);
        String message = log.getMessage();

        int start = message.indexOf('"') + MANGA_INDICATOR.length() + 1;
        int end = message.lastIndexOf('\"');

        String json = message.substring(start, end);
        json = removeBackslash(json);
        LOGGER.debug("Manga json: {}", json);
        StringReader sr = new StringReader(json);

        return (JsonObject) JsonTreeReader.read(sr);
    }

    protected LogEntry getFirstMatching(ChromeDriver driver, Predicate<LogEntry> predicate, long timeout) throws WebsiteException {
        long start = System.currentTimeMillis();

        while (start + timeout > System.currentTimeMillis()) {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);

            for (LogEntry entry : entries) {
                LOGGER.debug(entry);
                if (predicate.test(entry)) {
                    return entry;
                }
            }
        }

        throw new WebsiteException("Timeout. Failed to get log matching predicate");
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
    protected Path getOutputFile(Chapter entry, Settings settings) {
        String fileName = entry.manga() + " - " + entry.chapter() + ".pdf";

        if (settings.out() != null) {
            return settings.out().resolve(fileName);
        } else {
            return Path.of(fileName);
        }
    }

    @Override
    protected boolean preDownload(List<Chapter> entries, Settings settings) throws Throwable {
        if (entries.size() == 1) {
            sw = AbstractScanWriter.newWriter(null, false, settings.out());
        } else {
            sw = AbstractScanWriter.newWriter(entries.get(0).manga(),
                    settings.concatenateAll(),
                    settings.out());
        }

        return Main.noOverwrites.isNotPresent() || !settings.concatenateAll() || !Files.exists(sw.allPath());
    }

    @Override
    protected void postDownload(List<Chapter> entries, Settings settings) throws Throwable {
        sw.endAll();
    }

    @Override
    protected void processEntry(Chapter entry, Settings settings) throws Throwable {
        Set<Cookie> cookieSet = WebDriver.get().manage().getCookies();

        HttpHeaders headers = standardHeaders()
                .header("a", "aa37ec6b0977")
                .header("referer", "https://www.japanread.cc/manga/kaifuku-jutsushi-yarinaoshi/0")
                .header("x-requested-with", "XMLHttpRequest");

        for (Cookie c : cookieSet) {
            headers.header("cookie", c.getName() + "=" + c.getValue());
        }

        JsonObject object = (JsonObject) getJson(
                "https://www.japanread.cc/api/?id=%s&type=chapter".formatted(entry.url()),
                headers);

        String baseURL = url() + object.getAsJsonString("baseImagesUrl").getAsString() + "/";
        List<String> pages = new ArrayList<>();

        for (JsonElement e : object.getAsArray("page_array")) {
            String page = ((JsonValue) e).getAsString();

            pages.add(baseURL + page);
        }

        sw.newScan(entry.manga() + " - " + entry.chapter());
        downloadPDF(sw, pages);
        sw.endScan();
    }

    @Override
    public String name() {
        return "japanread";
    }

    @Override
    public String version() {
        return "28.02.2022";
    }

    @Override
    public String url() {
        return "https://www.japanread.cc";
    }

    @Override
    public List<DefaultTitle> search(String search, Settings settings) throws Throwable {
        List<DefaultTitle> titles = new ArrayList<>();
        int page = 1;

        while (true) {
            String r = "https://www.japanread.cc/manga-list?q=%s%s";
            if (page > 1) {
                r = r.formatted(search, "&page=" + page);
            } else {
                r = r.formatted(search, "");
            }

            Document document = getDocument(r);

            Elements elements = document.getElementsByClass("ml-1 text-truncate font-weight-bold");
            for (Element element : elements) {
                String url = url() + element.attr("a");
                String title = element.attr("title");

                titles.add(new DefaultTitle(url, title, this));
            }

            Element pagination = document.selectFirst(".pagination");

            if (pagination != null) {
                Element e = pagination.children().last();

                if (e.hasClass("disabled")) {
                    break;
                }
            } else {
                break;
            }

            page++;
        }

        return titles;
    }
}
