package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.scan.AbstractSimpleScanWebsite;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.HttpQueryParameterBuilder;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.DOMException;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilterList;
import fr.poulpogaz.json.JsonException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MangaRead extends AbstractSimpleScanWebsite<Manga, Chapter> implements SearchWebsite<Manga> {

    @Override
    public String name() {
        return "mangaread";
    }

    @Override
    public String version() {
        return "dev2";
    }

    @Override
    public String url() {
        return "https://www.mangaread.org";
    }

    @Override
    public boolean isChapterURL(String url) {
        Pattern pattern = Pattern.compile("^https://www.mangaread.org/manga/.+/.+$");

        return pattern.matcher(url).find();
    }

    @Override
    public boolean isMangaURL(String url) {
        Pattern pattern = Pattern.compile("^https://www.mangaread.org/manga/.+/$");

        return pattern.matcher(url).find() && !isChapterURL(url);
    }

    @Override
    public Manga getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        String mangaURL = getMangaURL(url);
        Document doc = getDocument(mangaURL);

        Manga.Builder builder = new Manga.Builder();
        builder.setUrl(mangaURL);

        Element title = selectNonNull(doc, ".post-title > h1");
        builder.setTitle(title.html());

        builder.setAuthor(parseList(doc, ".author-content").collect(Collectors.joining(", ")));
        builder.setArtist(parseList(doc, ".artist-content").collect(Collectors.joining(", ")));
        builder.setGenres(parseList(doc, ".genres-content").toList());
        builder.setDescription(getDescription(doc));

        // quite angry selector
        Element status = selectNonNull(doc, ".post-status > .post-content_item > .summary-content:not(:has(a))");
        builder.setStatus(parseStatus(status.html()));

        Element image = selectNonNull(doc, ".summary_image > a > img");
        builder.setThumbnailURL(image.attr("data-src"));

        Element score = selectNonNull(doc, ".post-total-rating > .total_votes");
        builder.setScore(Float.parseFloat(score.html()));

        return builder.build();
    }

    private String getMangaURL(String url) throws UnsupportedURLException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            return Utils.getRegexGroup(url, "^(https://www.mangaread.org/manga/.+/).+$");
        } else {
            throw new UnsupportedURLException(this, url);
        }
    }

    private Stream<String> parseList(Element document, String selector) {
        Elements elements = document.select(selector + " > a");

        return elements.stream()
                .map(Element::text);
    }

    private String getDescription(Document document) throws WebsiteException {
        Element summary = selectNonNull(document, ".summary__content");

        StringBuilder description = new StringBuilder();

        List<Node> childNodes = summary.childNodes();
        for (int i = 0; i < childNodes.size(); i++) {
            Node child = childNodes.get(i);

            if (child instanceof Element e && e.is("p")) {
                description.append(e.html());
            } else if (child instanceof TextNode t) {
                description.append(t.text());
            }

            if (i + 1 < childNodes.size()) {
                description.append('\n');
            }
        }

        return description.toString();
    }

    private Status parseStatus(String status) {
        return switch (status) {
            case "Completed" -> Status.COMPLETED;
            case "OnGoing" -> Status.ONGOING;
            case "Canceled" -> Status.CANCELED;
            default -> null;
        };
    }

    @Override
    public List<Chapter> getChapters(Manga manga)
            throws IOException, InterruptedException, WebsiteException {
        Document doc = getDocument(manga.getUrl());
        Elements chapters = doc.select(".wp-manga-chapter > a");

        Chapter.Builder builder = new Chapter.Builder();
        builder.setManga(manga);

        List<Chapter> entries = new ArrayList<>();
        for (Element chapter : chapters) {
            Element a = selectNonNull(chapter, "a");
            int i = Utils.getFirstInt(a.html());

            builder.setUrl(a.attr("href"));
            builder.setChapterNumber(i);
            builder.setName(a.html());

            entries.add(builder.build());
        }

        return Collections.unmodifiableList(entries);
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException {
        return new StringPageIterator(chapter);
    }

    private class StringPageIterator implements PageIterator<String> {

        private final Elements elements;
        private int index = 0;

        public StringPageIterator(Chapter chapter) throws IOException, InterruptedException {
            Document document = getDocument(chapter.getUrl());

            elements = document.select(".wp-manga-chapter-img");
        }

        @Override
        public boolean hasNext() {
            return index < elements.size();
        }

        @Override
        public String next() throws WebsiteException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Element e = elements.get(index);
            String url = e.attr("data-src").replace("\t", "").replace("\n", "");

            index++;

            return url;
        }

        @Override
        public Optional<Integer> nPages() {
            return Optional.of(elements.size());
        }
    }


    @Override
    public FilterList getSearchFilter() {
        return new UrlFilterList.Builder()
                .group("Genres", "genre[]")
                    .checkBox("Action", "action")
                    .checkBox("Adventure", "adventure")
                    .checkBox("Animated", "animated")
                    .checkBox("Anime", "anime")
                    .checkBox("Cartoon", "cartoon")
                    .checkBox("Comedy", "comedy")
                    .checkBox("Comic", "comic")
                    .checkBox("Completed", "completed")
                    .checkBox("Cooking", "cooking")
                    .checkBox("Detective", "detective")
                    .checkBox("Drama", "drama")
                    .checkBox("Ecchi", "ecchi")
                    .checkBox("Fantasy", "fantasy")
                    .checkBox("Gender Bender", "gender-bender")
                    .checkBox("Harem", "harem")
                    .checkBox("Historical", "historical")
                    .checkBox("Horror", "horror")
                    .checkBox("Isekai", "isekai")
                    .checkBox("Josei", "josei")
                    .checkBox("Magic", "magic")
                    .checkBox("Manga", "manga")
                    .checkBox("Manhua", "manhua")
                    .checkBox("Manwha", "mawha")
                    .checkBox("Martial Arts", "martial-arts")
                    .checkBox("Mature", "mature")
                    .checkBox("Mecha", "mecha")
                    .checkBox("Military", "military")
                    .checkBox("Mistery", "mistery")
                    .checkBox("One shot", "one-shot")
                    .checkBox("Psychological", "psycological")
                    .checkBox("Reincarnation", "reincarnation")
                    .checkBox("Romance", "romance")
                    .checkBox("School Life", "school-life")
                    .checkBox("Sci-fi", "sci-fi")
                    .checkBox("Seinen", "seinen")
                    .checkBox("Shoujo", "shouko")
                    .checkBox("Shoujo Ai", "shoujo-ai")
                    .checkBox("Slice of Life", "slice-of-life")
                    .checkBox("Smut", "smut")
                    .checkBox("Sports", "sports")
                    .checkBox("Super Power", "super-power")
                    .checkBox("Supernatural", "supernatural")
                    .checkBox("Tragedy", "tragedy")
                    .checkBox("Webtoon", "webtoon")
                    .build()

                .select("Genres conditions", "op")
                    .addVal("OR")
                    .add("AND", "1")
                    .build()

                .text("Author", "author")
                .text("Artist", "artist")
                .text("Year of Released", "release")

                .select("Adult content")
                    .addVal("All")
                    .add("None adult content", "0")
                    .add("Only adult content", "1")
                .build()

                .group("Status", "status[]")
                    .checkBox("OnGoing", "on-going")
                    .checkBox("Completed", "completed")
                    .checkBox("Canceled", "canceled")
                    .checkBox("On Hold", "on-hold")
                    .checkBox("Upcoming", "upcoming")
                    .build()

                .select("Order", "m_orderby")
                    .add("Revelance", null)
                    .add("Lastest", "latest")
                    .add("A-Z", "alphabet")
                    .add("Rating", "rating")
                    .add("Trending", "trending")
                    .add("Most Views", "views")
                    .add("New", "new-manga")
                .build()
                .build();
    }

    @Override
    public List<Manga> search(String search, FilterList filter)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        if (filter instanceof UrlFilterList urlFilterList) {
            List<Manga> mangas = new ArrayList<>();

            int page = 1;
            int nResult = Integer.MAX_VALUE;
            while (mangas.size() < Math.min(nResult, filter.getLimit())) {
                nResult = search(page, search, urlFilterList, mangas);
                page++;
            }

            return mangas;
        }

        return List.of();
    }

    private int search(int page, String search, UrlFilterList urlFilterList, List<Manga> out)
            throws IOException, InterruptedException, WebsiteException {
        HttpQueryParameterBuilder b = new HttpQueryParameterBuilder();
        b.add("s", Objects.requireNonNullElse(search, ""));
        b.add("post_type", "wp-manga");

        String url = urlFilterList.createRequest(url() + "/page/" + page + "/", b);

        Document document = getDocument(url);
        Elements mangas = document.select("[role=tabpanel] > .row.c-tabs-item__content");

        int max = Math.min(mangas.size(), urlFilterList.getLimit() - out.size());
        for (int i = 0; i < max; i++) {
            out.add(getMangaFromSearch(mangas.get(i)));
        }

        Element e = selectNonNull(document, ".c-blog__heading.style-2.font-heading > .h4");
        String text = Utils.getFirstNonEmptyText(e, true);

        return Utils.getFirstInt(text);
    }

    private Manga getMangaFromSearch(Element e) throws DOMException {
        // .col-4.col-12.col-md-2 > .tab-thumb.c-image-hover > a
        Manga.Builder builder = new Manga.Builder();
        Element url = selectNonNull(e, ".c-image-hover > a");
        builder.setUrl(url.attr("href"));
        builder.setThumbnailURL(selectNonNull(url, "img").attr("data-src"));
        builder.setTitle(selectNonNull(e, ".h4").text());
        builder.setAuthor(parseList(e, ".mg_author > .summary-content").collect(Collectors.joining(", ")));
        builder.setArtist(parseList(e, ".mg_artists > .summary-content").collect(Collectors.joining(", ")));
        builder.setGenres(parseList(e, ".mg_genres > .summary-content").toList());
        builder.setStatus(parseStatus(selectNonNull(e, ".mg_status > .summary-content").text()));

        return builder.build();
    }
}
