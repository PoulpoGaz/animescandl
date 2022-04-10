package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.DefaultTitle;
import fr.poulpogaz.animescandl.model.Entry;
import fr.poulpogaz.animescandl.utils.AbstractScanWriter;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import fr.poulpogaz.json.tree.value.JsonString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @version 27.02.2022
 */
public class SushiScan extends AbstractWebsite<Chapter, DefaultTitle> {

    public static final SushiScan INSTANCE = new SushiScan();

    private static final Logger LOGGER = LogManager.getLogger(SushiScan.class);

    private static final String SEARCH_REQUEST = "https://sushi-scan.su/wp-admin/admin-ajax.php";
    private static final String SEARCH_PARAMETER = "action=ts_ac_do_search&ts_ac_query=%s";

    private AbstractScanWriter sw;

    private SushiScan() {

    }

    @Override
    public String name() {
        return "Sushi-Scan";
    }

    @Override
    public String version() {
        return "27.02.2022";
    }

    @Override
    public String url() {
        return "https://sushi-scan.su/";
    }

    @Override
    protected List<Chapter> fetchList(String url, Settings settings) throws Throwable {
        if (settings.range() == null && isChapter(url)) {
            return List.of(new Chapter(url, 0, null, null, null));
        } else {
            String mangaURL = getMangaURL(url);

            Document doc = getDocument(mangaURL);
            Element element = doc.selectFirst("#chapterlist > ul");
            Element series = doc.getElementsByClass("entry-title").first();

            if (element == null || series == null) {
                throw new IOException("Can't find chapter list/anime title in manga page");
            }

            String title = series.html();

            List<Chapter> chapters = new ArrayList<>();
            for (Element sub : element.children()) {
                String dataNum = sub.attr("data-num");

                int i = Utils.getFirstInt(dataNum);

                if (settings.rangeContains(i)) {
                    Element a = sub.selectFirst("a");
                    if (a == null) {
                        throw new IOException("Can't find <a>");
                    }
                    chapters.add(new Chapter(a.attr("href"), i, title, null, null));
                }
            }

            chapters.sort(Comparator.comparingInt(Entry::index));

            return chapters;
        }
    }

    protected String getMangaURL(String url) throws IOException, InterruptedException, WebsiteException {
        if (isMangaPage(url)) {
            return url;
        } else if (isChapter(url)) {
            Document document = getDocument(url);
            Element element = document.selectFirst("div.allc > a");

            if (element == null) {
                throw new IOException("Invalid SushiScan link");
            }

            return element.attr("href");
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }
    }

    private boolean isChapter(String url) {
        Pattern pattern = Pattern.compile("^https://sushi-scan\\.su/.+/$");

        return pattern.matcher(url).find() && !isMangaPage(url);
    }

    private boolean isMangaPage(String url) {
        Pattern pattern = Pattern.compile("sushi-scan\\.su/manga/\\S*/");

        return pattern.matcher(url).find();
    }

    @Override
    protected Path getOutputFile(Chapter entry, Settings settings) {
        return null;
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

        return !Main.noOverwrites.isPresent() || !settings.concatenateAll() || !Files.exists(sw.allPath());
    }

    @Override
    protected void postDownload(List<Chapter> entries, Settings settings) throws Throwable {
       sw.endAll();
       sw = null;
    }

    @Override
    protected void processEntry(Chapter chapter, Settings settings) throws IOException, InterruptedException {
        Document document = getDocument(chapter.url());
        String name = document.getElementsByClass("entry-title").get(0).html();

        if (Main.noOverwrites.isPresent() && exists(name, settings)) {
            return;
        }

        LOGGER.info("Downloading {}", chapter.url());
        Elements scripts = document.select("script");

        sw.newScan(name);
        for (Element element : scripts) {
            if (element.html().startsWith("ts_reader.run({\"post_id\":")) {
                List<String> pages = extractPages(element);

                if (pages != null) {
                    downloadPDF(sw, pages);
                }

                break;
            }
        }
        sw.endScan();
    }

    private boolean exists(String fileName, Settings settings) {
        Path out;
        if (settings.out() != null) {
            out = settings.out().resolve(fileName + ".pdf");
        } else {
            out = Path.of(fileName + ".pdf");
        }

        return Files.exists(out);
    }

    private List<String> extractPages(Element element) {
        String html = element.html();
        int start = html.indexOf('{');
        int end = html.lastIndexOf('}') + 1;

        String json = html.substring(start, end);

        try {
            JsonObject object = (JsonObject) JsonTreeReader.read(new StringReader(json));
            JsonArray sources = object.getAsArray("sources");
            if (sources == null) {
                return null;
            }
            JsonObject source = sources.getAsObject(0);
            if (source == null) {
                return null;
            }
            JsonArray images = source.getAsArray("images");
            if (images == null) {
                return null;
            }

            List<String> pages = new ArrayList<>();

            for (JsonElement e : images) {
                pages.add(((JsonString) e).getAsString());
            }

            return pages;
        } catch (IOException | JsonException e) {
            LOGGER.warn("Failed to extract pages", e);

            return null;
        }

    }

    // **********
    // * SEARCH *
    // **********

    @Override
    public List<DefaultTitle> search(String search, Settings settings) throws Throwable {
        InputStream is = getInputStream(standardRequest(SEARCH_REQUEST)
                .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(SEARCH_PARAMETER.formatted(search)))
                .build());

        JsonObject object = (JsonObject) JsonTreeReader.read(is);
        JsonObject object2 = (JsonObject) object.getAsArray("series").get(0);
        JsonArray all = object2.getAsArray("all");

        List<DefaultTitle> results = new ArrayList<>();

        for (JsonElement e : all) {
            JsonObject o = (JsonObject) e;
            String url = o.getAsJsonString("post_link").getAsString();
            String title = o.getAsJsonString("post_title").getAsString();

            results.add(new DefaultTitle(url, title, this));
        }

        return Collections.unmodifiableList(results);
    }
}
