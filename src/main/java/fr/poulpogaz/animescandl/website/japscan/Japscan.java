package fr.poulpogaz.animescandl.website.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.AbstractScanWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.iterators.BufferedImagePageIterator;
import fr.poulpogaz.animescandl.website.iterators.InputStreamPageIterator;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;
import org.checkerframework.checker.units.qual.C;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Japscan extends AbstractScanWebsite<Manga, Chapter> {

    @Override
    public String name() {
        return "japscan";
    }

    @Override
    public String version() {
        return "dev1";
    }

    @Override
    public String url() {
        return "https://www.japscan.ws";
    }

    @Override
    public boolean supportHeadless() {
        return false;
    }

    // https://www.japscan.ws/manga/owari-no-seraph/
    @Override
    public boolean isChapterURL(String url) {
        return url.contains("japscan.ws/lecture-en-ligne/");
    }

    // https://www.japscan.ws/lecture-en-ligne/owari-no-seraph/1/5.html
    @Override
    public boolean isMangaURL(String url) {
        return url.contains("japscan.ws/manga/");
    }

    @Override
    public Manga getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        Manga.Builder builder = new Manga.Builder();

        String mangaURL = getMangaUrl(url);

        Document doc = getDocument(mangaURL);
        String title = doc.title().replace("Manga ", "").replace(" | JapScan", "");

        builder.setUrl(mangaURL);
        builder.setTitle(title);

        Elements elements = doc.select(".mb-2");
        for (Element element : elements) {
            Element span = element.selectFirst("span");

            switch (span.html()) {
                case "Statut:" -> {
                    builder.setStatus(parseStatus(getPMB_2_String(element)));
                }
                case "Genre(s):" -> {
                    List<String> genres = parseMB_2_List(element);
                    builder.setGenres(genres);
                }
                case "Artiste(s):" -> {
                    builder.setArtist(getPMB_2_String(element));
                }
                case "Auteur(s):" -> {
                    builder.setAuthor(getPMB_2_String(element));
                }
            }
        }

        Element thumbnail = doc.selectFirst(".m-2 > img");
        builder.setThumbnailURL(url() + thumbnail.attr("src"));

        return builder.build();
    }

    protected String getMangaUrl(String url) throws WebsiteException, IOException, InterruptedException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            Document document = getDocument(url);

            Element e = document.getElementsByClass("breadcrumb justify-content-center").first();
            Element link = e.child(2).selectFirst("a");

            return url() + link.attr("href");
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }
    }

    private Status parseStatus(String text) {
        return switch (text) {
            case "Terminé" -> Status.COMPLETED;
            case "En Cours" -> Status.ONGOING;
            case "En Pause" -> Status.HIATUS;
            default -> null;
        };
    }

    private List<String> parseMB_2_List(Element pmb_2) {
       String text = getPMB_2_String(pmb_2);

        return List.of(text.split(", "));
    }

    private String getPMB_2_String(Element pmb_2) {
        for (TextNode n : pmb_2.textNodes()) {
            String text = n.text();

            if (!text.isBlank() && !text.isEmpty()) {
                return text.substring(1, text.length() - 1);
            }
        }

        return "";
    }

    @Override
    public List<Chapter> getChapters(Manga manga)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        Document document = getDocument(manga.getUrl());
        Element chaptersList = document.getElementById("chapters_list");

        List<Chapter> chapters = new ArrayList<>();
        int volume = -1;
        for (Element child : chaptersList.children()) {
            if (child.is("h4")) {
                String v = child.getElementsByTag("span")
                        .first()
                        .text();

                volume = Utils.getFirstInt(v);
            } else if (child.is("div")) {

                for (Element e : child.select("a.text-dark")) {
                    String link = url() + e.attr("href");
                    int index = Utils.getFirstInt(e.text());

                    Chapter.Builder builder = new Chapter.Builder();
                    builder.setUrl(link);
                    builder.setChapterNumber(index);
                    builder.setVolume(volume);
                    builder.setName(e.text());

                    chapters.add(builder.build());
                }

            }
        }

        return chapters;
    }

    @Override
    public Class<?>[] supportedIterators() {
        return new Class<?>[] {String.class, InputStream.class, BufferedImage.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> PageIterator<P> getPageIterator(Chapter chapter, Class<P> out)
            throws IOException, InterruptedException, WebsiteException {
        if (out == String.class) {
            PageIterator<String> iterator = new StringPageIterator(this, chapter);

            return (PageIterator<P>) iterator;
        } else if (out == InputStream.class) {
            return (PageIterator<P>) new InputStreamIterator(this, chapter);
        } else if (out == BufferedImage.class) {
            return (PageIterator<P>) new BufferedImagePageIterator(
                    new InputStreamIterator(this, chapter));
        }

        throw new WebsiteException("Unsupported page iterator");
    }

    @Override
    public List<Manga> search() {
        return null;
    }
}
