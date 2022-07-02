package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.scan.iterators.BufferedImagePageIterator;
import fr.poulpogaz.animescandl.scan.iterators.InputStreamPageIterator;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilterList;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Japscan extends AbstractWebsite implements ScanWebsite, SearchWebsite<Manga> {

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
    public boolean needCEF() {
        return true;
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
            Element span = selectNonNull(element, "span");

            switch (span.html()) {
                case "Statut:" -> {
                    builder.setStatus(parseStatus(Utils.getFirstNonEmptyText(element, true)));
                }
                case "Genre(s):" -> {
                    List<String> genres = parseMB_2_List(element);
                    builder.setGenres(genres);
                }
                case "Artiste(s):" -> {
                    builder.setArtist(Utils.getFirstNonEmptyText(element, true));
                }
                case "Auteur(s):" -> {
                    builder.setAuthor(Utils.getFirstNonEmptyText(element, true));
                }
            }
        }

        Element thumbnail = selectNonNull(doc, ".m-2 > img");
        builder.setThumbnailURL(url() + thumbnail.attr("src"));

        return builder.build();
    }

    protected String getMangaUrl(String url) throws WebsiteException, IOException, InterruptedException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            Document document = getDocument(url);

            Element e = selectNonNull(document, ".breadcrumb.justify-content-center");
            Element link = selectNonNull(child(e, 2), "a");

            return url() + link.attr("href");
        } else {
            throw new UnsupportedURLException(this, "Unsupported url: " + url);
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
       String text = Utils.getFirstNonEmptyText(pmb_2, true);

        return List.of(text.split(", "));
    }

    @Override
    public List<Chapter> getChapters(Manga manga) throws IOException, InterruptedException, WebsiteException {
        Document document = getDocument(manga.getUrl());
        Element chaptersList = getElementById(document, "chapters_list");

        Chapter.Builder builder = new Chapter.Builder();
        builder.setManga(manga);

        List<Chapter> chapters = new ArrayList<>();
        for (Element child : chaptersList.children()) {
            if (child.is("h4")) {
                String v = first(child.getElementsByTag("span"))
                        .text();

                builder.setVolume(Utils.getFirstInt(v));
            } else if (child.is("div")) {

                for (Element e : child.select("a.text-dark")) {
                    String link = url() + e.attr("href");
                    int index = Utils.getFirstInt(e.text());

                    builder.setUrl(link);
                    builder.setChapterNumber(index);
                    builder.setName(e.text());

                    chapters.add(builder.build());
                }

            }
        }

        return Collections.unmodifiableList(chapters);
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
            return (PageIterator<P>) new InputStreamPageIterator(
                    new StringPageIterator(this, chapter), this);
        } else if (out == BufferedImage.class) {
            return (PageIterator<P>) new BufferedImagePageIterator(
                    new InputStreamPageIterator(
                            new StringPageIterator(this, chapter), this));
        }

        throw new WebsiteException("Unsupported page iterator");
    }

    @Override
    public FilterList getSearchFilter() {
        return new UrlFilterList();
    }

    @Override
    public List<Manga> search(String search, FilterList filter)
            throws JsonException, IOException, InterruptedException {
        HttpRequest r = standardRequest(url() + "/live-search/")
                .header("x-requested-with", "XMLHttpRequest")
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("search=" + search + "&limit=1"))
                .build();

        JsonArray array = (JsonArray) getJson(r);

        Manga.Builder builder = new Manga.Builder();
        List<Manga> titles = new ArrayList<>();
        for (JsonElement e : array) {
            JsonObject o = (JsonObject) e;

            builder.setUrl(url() + o.getAsString("url"));
            builder.setTitle(o.getAsString("name"));

            String author = o.getOptionalString("mangakas").orElse(null);
            builder.setAuthor(author);
            builder.setArtist(author);

            titles.add(builder.build());
        }

        return titles;
    }
}
