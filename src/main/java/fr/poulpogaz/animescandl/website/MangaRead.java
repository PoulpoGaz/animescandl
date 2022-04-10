package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.DefaultEntry;
import fr.poulpogaz.animescandl.model.DefaultTitle;
import fr.poulpogaz.animescandl.model.Entry;
import fr.poulpogaz.animescandl.utils.AbstractScanWriter;
import fr.poulpogaz.animescandl.utils.Utils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangaRead extends AbstractWebsite<Chapter, DefaultTitle> {

    public static final MangaRead INSTANCE = new MangaRead();

    private AbstractScanWriter sw;

    private MangaRead() {

    }

    @Override
    protected List<Chapter> fetchList(String url, Settings settings) throws Throwable {
        if (settings.range() == null && isChapterPage(url)) {
            throw new UnsupportedOperationException();
            //return List.of(new Chapter(url, 0, null, null, null));
        } else {
            String mangaURL = getMangaURL(url);

            Document doc = getDocument(mangaURL);
            Elements chapters = doc.select(".wp-manga-chapter > a");
            Element title = doc.selectFirst(".post-title > h1");

            if (chapters.isEmpty() || title == null) {
                throw new IOException("Can't find chapter list/anime title in manga page");
            }

            String t = title.html();

            List<Chapter> entries = new ArrayList<>();
            for (Element chapter : chapters) {
                Element a = chapter.selectFirst("a");
                int i = Utils.getFirstInt(a.html());

                entries.add(new Chapter(a.attr("href"), i, t, null, a.html()));
            }

            entries.sort(Comparator.comparingInt(Entry::index));

            return entries;
        }
    }

    private boolean isChapterPage(String url) {
        Pattern pattern = Pattern.compile("^https://www.mangaread.org/manga/.+/.+$");

        return pattern.matcher(url).find();
    }

    private boolean isMangaPage(String url) {
        Pattern pattern = Pattern.compile("^https://www.mangaread.org/manga/.+/$");

        return pattern.matcher(url).find() && !isChapterPage(url);
    }

    private String getMangaURL(String chapterURL) {
        return Utils.getRegexGroup(chapterURL, "^(https://www.mangaread.org/manga/.+/).+$");
    }

    @Override
    protected Path getOutputFile(Chapter entry, Settings settings) {
        String fileName = entry.getChapterPDFName() + ".pdf";

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
        Document document = getDocument(entry.url());

        Elements elements = document.select(".wp-manga-chapter-img");
        List<String> pages = new ArrayList<>();
        for (Element e : elements) {
            String replace = e.attr("data-src").replace("\t", "").replace("\n", "");
            pages.add(replace);
        }

        sw.newScan(entry.getChapterPDFName());
        downloadPDF(sw, pages);
        sw.endScan();
    }

    @Override
    public String name() {
        return "mangaread";
    }

    @Override
    public String version() {
        return "dev";
    }

    @Override
    public String url() {
        return "https://www.mangaread.org/";
    }

    @Override
    public List<DefaultTitle> search(String search, Settings settings) throws Throwable {
        return null;
    }
}
