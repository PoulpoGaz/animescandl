package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.DefaultEntry;
import fr.poulpogaz.animescandl.model.DefaultTitle;
import fr.poulpogaz.animescandl.utils.AbstractScanWriter;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebDriver;
import fr.poulpogaz.animescandl.utils.WebsiteException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.opera.OperaDriver;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 27.02.2022
 */
public class Japscan extends AbstractWebsite<Japscan.JapsanChapter, DefaultTitle> {

    private static final Logger LOGGER = LogManager.getLogger(Japscan.class);

    public static final Japscan INSTANCE = new Japscan();
    private AbstractScanWriter sw = null;

    private Japscan() {

    }

    @Override
    protected List<JapsanChapter> fetchList(String url, Settings settings) throws Throwable {
        if (settings.range() == null && isChapter(url)) {
            String name = getChapterName(url);

            return List.of(new JapsanChapter(url, 0, null, null, name));
        } else {
            List<JapsanChapter> chapters = new ArrayList<>();

            String mangaURL = getMangaUrl(url);
            LOGGER.debug("Manga url: {}", mangaURL);

            Document document = getDocument(mangaURL);
            String manga = document.title().replace("Manga ", "").replace(" | JapScan", "");
            Element chaptersList = document.getElementById("chapters_list");

            String volume = null;
            for (Element child : chaptersList.children()) {
                if (child.is("h4")) {
                    volume = child.getElementsByTag("span")
                            .first()
                            .text();

                } else if (child.is("div")) {

                    for (Element e : child.select("a.text-dark")) {
                        String link = url() + e.attr("href");
                        int index = Utils.getFirstInt(e.text());

                        chapters.add(new JapsanChapter(link, index, manga, volume, e.text()));
                    }

                } else {
                    LOGGER.debug("Unknown child: {}", child);
                }
            }

            chapters.sort((c1, c2) -> {
                int a = Utils.getFirstInt(c1.chapter());
                int b = Utils.getFirstInt(c2.chapter());

                return Integer.compare(a, b);
            });

            return chapters;
        }
    }
    protected String getMangaUrl(String url) throws WebsiteException, IOException, InterruptedException {
        if (isMangaPage(url)) {
            return url;
        } else if (isChapter(url)) {
            Document document = getDocument(url);

            Element e = document.getElementsByClass("breadcrumb justify-content-center").first();
            Element link = e.child(2).selectFirst("a");

            return url() + link.attr("href");
        }

        throw new WebsiteException("Unsupported url: " + url);
    }

    // https://www.japscan.ws/manga/owari-no-seraph/
    protected boolean isMangaPage(String url) {
        return url.contains("japscan.ws/manga/");
    }

    // https://www.japscan.ws/lecture-en-ligne/owari-no-seraph/1/5.html
    protected boolean isChapter(String url) {
        return url.contains("japscan.ws/lecture-en-ligne/");
    }

    protected String getChapterName(String url) throws IOException, InterruptedException {
        Document document = getDocument(url);

        return document.getElementsByClass("text-center mt-2 font-weight-bold")
                .first()
                .text();
    }

    @Override
    protected Path getOutputFile(JapsanChapter entry, Settings settings) {
        if (settings.out() != null) {
            return settings.out().resolve(entry.getChapterPDFName() + ".pdf");
        } else {
            return Path.of(entry.getChapterPDFName() + ".pdf");
        }
    }

    @Override
    protected boolean preDownload(List<JapsanChapter> entries, Settings settings) throws Throwable {
        if (entries.size() == 1) {
            sw = AbstractScanWriter.newWriter(null, false, settings.out());
        } else {
            sw = AbstractScanWriter.newWriter(entries.get(0).manga(), settings.concatenateAll(), settings.out());
        }

        return !Main.noOverwrites.isPresent() || !settings.concatenateAll() || !Files.exists(sw.allPath());
    }

    @Override
    protected void postDownload(List<JapsanChapter> entries, Settings settings) throws Throwable {
        sw.endAll();
        sw = null;
    }

    @Override
    protected void processEntry(JapsanChapter entry, Settings settings) throws Throwable {
        List<String> pages = getPages(entry.url());

        LOGGER.debug(pages);

        sw.newScan(entry.getChapterPDFName());
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            printErase("Page %d/%d".formatted(i + 1, pages.size()));
            getImage(page);
        }
        sw.endScan();

        if (Main.verbose.isNotPresent()) {
            System.out.println();
        }
    }

    protected List<String> getPages(String entry) throws IOException, InterruptedException {
        Document document = getDocument(entry);
        Elements options = document.select("#pages > option");

        return options.stream().map((e) -> url() + e.attr("value")).toList();
    }

    /**
     * https://stackoverflow.com/questions/62799036/unable-to-locate-the-sign-in-element-within-shadow-root-open-using-selenium-a
     */
    protected void getImage(String url) throws IOException {
        OperaDriver driver = WebDriver.init(PageLoadStrategy.NORMAL);
        driver.get(url);

        // remove all div
        List<?> v = (List<?>) driver.executeScript("""
                var canvas = document.querySelector("cnv-vv");
                
                if (!canvas) {
                    return null;
                }
        
                document.body.textContent = '';

                var iframes = document.getElementsByTagName('iframe');
                console.log(iframes.length);
                for (var i = iframes.length - 1; i >= 0; i--) {
                    iframes[i].parentNode.removeChild(iframes[i]);
                }
                
                document.body.appendChild(canvas);
                
                return [canvas.width, canvas.height];
                """);

        if (v == null) {
            if (Main.verbose.isNotPresent()) {
                System.out.println();
            }
            LOGGER.warn("Empty page at {}", url);
            return;
        }

        int width = (int) (long) v.get(0);
        int height = (int) (long) v.get(1);

        LOGGER.debug("{}x{}", width, height);

        driver.manage().window().setSize(new Dimension(width, height));

        byte[] png = driver.getScreenshotAs(OutputType.BYTES);

        sw.addPage(png);
    }

    @Override
    public String name() {
        return "japscan";
    }

    @Override
    public String version() {
        return "27.02.2022";
    }

    @Override
    public String url() {
        return "https://www.japscan.ws";
    }

    @Override
    public List<DefaultTitle> search(String search, Settings settings) throws Throwable {
        HttpRequest r = standardRequest(url() + "/live-search/")
                .header("x-requested-with", "XMLHttpRequest")
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("search=" + search))
                .build();


        JsonArray array = (JsonArray) getJson(r);

        List<DefaultTitle> titles = new ArrayList<>();
        for (JsonElement e : array) {
            JsonObject o = (JsonObject) e;

            String url = url() + o.getAsJsonString("url").getAsString();
            String name = o.getAsJsonString("name").getAsString();

            titles.add(new DefaultTitle(url, name, this));
        }

        return titles;
    }

    protected static class JapsanChapter extends DefaultEntry {

        private final String manga;
        private final String volume;
        private final String chapter;

        public JapsanChapter(String url, int index, String manga, String volume, String chapter) {
            super(url, index);
            this.manga = manga;
            this.volume = volume;
            this.chapter = chapter;
        }

        public String getChapterPDFName() {
            StringBuilder builder = new StringBuilder();

            if (manga != null) {
                builder.append(manga);

                if (volume != null || chapter != null) {
                    builder.append(" - ");
                }
            }
            if (volume != null) {
                builder.append(volume);

                if (chapter != null) {
                    builder.append(" - ");
                }
            }
            if (chapter != null) {
                builder.append(chapter);
            }

            return builder.toString();
        }

        public String manga() {
            return manga;
        }

        public String volume() {
            return volume;
        }

        public String chapter() {
            return chapter;
        }

        @Override
        public String toString() {
            return "JapsanChapter{" +
                    "url='" + url + '\'' +
                    ", index=" + index +
                    ", manga='" + manga + '\'' +
                    ", volume='" + volume + '\'' +
                    ", chapter='" + chapter + '\'' +
                    '}';
        }
    }
}
