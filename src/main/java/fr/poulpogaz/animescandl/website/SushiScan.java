package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SushiScan extends AbstractSimpleScanWebsite<Manga, Chapter> {

    @Override
    public String name() {
        return "Sushi-Scan";
    }

    @Override
    public String url() {
        return "https://sushi-scan.su/";
    }

    @Override
    public String version() {
        return "dev";
    }

    @Override
    public boolean isChapterURL(String url) {
        Pattern pattern = Pattern.compile("^https://sushi-scan\\.su/.+/$");

        return pattern.matcher(url).find() && !isMangaURL(url);
    }

    @Override
    public boolean isMangaURL(String url) {
        Pattern pattern = Pattern.compile("sushi-scan\\.su/manga/\\S*/");

        return pattern.matcher(url).find();
    }

    @Override
    public Manga getManga(String url) throws IOException, InterruptedException, UnsupportedURLException {
        Manga.Builder builder = new Manga.Builder();

        String mangaURL = getMangaURL(url);
        builder.setUrl(mangaURL);

        Document document = getDocument(mangaURL);
        builder.setTitle(document.selectFirst(".entry-title").html());
        builder.setThumbnailURL(document.selectFirst(".thumb > img").attr("a"));

        float score = Utils.parseFloat(document.selectFirst(".num").html()).orElse(-1f);
        builder.setScore(score);

        Elements metadata = document.select(".tsinfo.bixbox > div > i");
        String status = metadata.get(0).html();
        builder.setStatus(parseStatus(status));

        builder.setAuthor(metadata.get(2).html());
        builder.setArtist(metadata.get(3).html());

        Elements genres = document.select(".mgen > a");
        for (Element e : genres) {
            builder.addGenre(e.html());
        }

        Elements description = document.select("[itemprop=description] > p");
        String desc = description.stream()
                .map(Element::html)
                .collect(Collectors.joining("\n"));
        builder.setDescription(desc);

        return builder.build();
    }

    protected String getMangaURL(String url) throws IOException, InterruptedException, UnsupportedURLException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            Document document = getDocument(url);
            Element element = document.selectFirst("div.allc > a");

            if (element == null) {
                throw new IOException("Invalid SushiScan link");
            }

            return element.attr("href");
        } else {
            throw new UnsupportedURLException(this, "Unsupported url: " + url);
        }
    }

    protected Status parseStatus(String status) {
        return switch (status) {
            case "En Cours" -> Status.ONGOING;
            case "Abandonné" -> Status.CANCELED;
            case "Terminé" -> Status.COMPLETED;
            default -> null;
        };
    }

    @Override
    public List<Chapter> getChapters(Manga manga) throws IOException, InterruptedException {
        Document doc = getDocument(manga.getUrl());
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

            Element a = sub.selectFirst("a");
            if (a == null) {
                throw new IOException("Can't find <a>");
            }
            chapters.add(new Chapter(a.attr("href"), i, title));
        }

        return chapters;
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, WebsiteException {
        return new StringPageIterator(chapter);
    }

    @Override
    public List<Manga> search() {
        return null;
    }


    private class StringPageIterator implements PageIterator<String> {

        private JsonArray images = null;
        private int index;

        public StringPageIterator(Chapter chapter) throws IOException, InterruptedException, WebsiteException {
            Document document = getDocument(chapter.getUrl());
            Elements scripts = document.select("script");

            for (Element element : scripts) {
                if (element.html().startsWith("ts_reader.run({\"post_id\":")) {
                    images = extractURLs(element);

                    break;
                }
            }

            if (images == null) {
                throw new WebsiteException("Can't find urls");
            }

            index = 0;
        }

        private JsonArray extractURLs(Element element) throws WebsiteException, IOException {
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

                return source.getAsArray("images");
            } catch (JsonException e) {
                throw new WebsiteException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return index < images.size();
        }

        @Override
        public String next() throws WebsiteException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Optional<String> url = images.getOptionalString(index);
            index++;

            return url.orElseThrow(WebsiteException::new);
        }

        @Override
        public Optional<Integer> nPages() {
            return Optional.of(images.size());
        }
    }
}
