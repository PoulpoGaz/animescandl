package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
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

public class MangaRead extends AbstractSimpleScanWebsite<Manga, Chapter> {

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
        return "https://www.mangaread.org/";
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
        builder.setGenres(parseList(doc, ".genres-content").collect(Collectors.toList()));
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

    private Stream<String> parseList(Document document, String selector) {
        Elements elements = document.select(selector + " > a");

        return elements.stream()
                .map(Element::html);
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

    @Override
    public List<Manga> search() {
        return null;
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
}
