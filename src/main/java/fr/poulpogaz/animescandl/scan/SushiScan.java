package fr.poulpogaz.animescandl.scan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
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
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SushiScan extends AbstractSimpleScanWebsite
        implements SearchWebsite<Manga> {

    @Override
    public String name() {
        return "sushiscan";
    }

    @Override
    public String url() {
        return "https://sushiscan.su";
    }

    @Override
    public String version() {
        return "30.06.2022";
    }

    @Override
    public boolean isChapterURL(String url) {
        Pattern pattern = Pattern.compile("^https://sushiscan\\.su/.+/$");

        return pattern.matcher(url).find() && !isMangaURL(url);
    }

    @Override
    public boolean isMangaURL(String url) {
        Pattern pattern = Pattern.compile("sushiscan\\.su/manga/\\S*/");

        return pattern.matcher(url).find();
    }

    @Override
    public Manga getManga(String url) throws IOException, InterruptedException, WebsiteException {
        Manga.Builder builder = new Manga.Builder();

        String mangaURL = getMangaURL(url);
        builder.setUrl(mangaURL);

        Document document = getDocument(mangaURL);
        builder.setTitle(selectNonNull(document, ".entry-title").html());
        builder.setThumbnailURL(selectNonNull(document, ".thumb > img").attr("a"));

        float score = Utils.parseFloat(selectNonNull(document, ".num").html()).orElse(-1f);
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

    protected String getMangaURL(String url) throws IOException, InterruptedException, WebsiteException {
        if (isMangaURL(url)) {
            return url;
        } else if (isChapterURL(url)) {
            Document document = getDocument(url);
            Element element = selectNonNull(document, "div.allc > a");

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
    public List<Chapter> getChapters(Manga manga) throws IOException, InterruptedException, WebsiteException {
        Document doc = getDocument(manga.getUrl());
        Element element = selectNonNull(doc, "#chapterlist > ul");

        Chapter.Builder builder = new Chapter.Builder();
        builder.setManga(manga);

        List<Chapter> chapters = new ArrayList<>();
        for (Element sub : element.children()) {
            String dataNum = sub.attr("data-num");
            builder.setName(dataNum);
            builder.setChapterNumber(Utils.getFirstInt(dataNum));

            Element a = selectNonNull(sub, "a");
            builder.setUrl(a.attr("href"));
            chapters.add(builder.build());
        }

        return Collections.unmodifiableList(chapters);
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, WebsiteException {
        return new StringPageIterator(chapter);
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

    @Override
    public FilterList getSearchFilter() {
        return new UrlFilterList.Builder()
                .group("Genres", "genre[]")
                    .checkBox("Action", "3")
                    .checkBox("Aventure", "12")
                    .checkBox("Biographique", "165")
                    .checkBox("Comédie", "13")
                    .checkBox("Crossover", "267")
                    .checkBox("Drame", "4")
                    .checkBox("Ecchi", "43")
                    .checkBox("Erotique", "96")
                    .checkBox("Fantastique", "5")
                    .checkBox("Fantasy", "32")
                    .checkBox("Histoires courtes", "93")
                    .checkBox("Historique", "24")
                    .checkBox("Horreur", "6")
                    .checkBox("Isekai", "639")
                    .checkBox("Mature", "7")
                    .checkBox("Mystère", "8")
                    .checkBox("Psychologique", "20")
                    .checkBox("Romance", "26")
                    .checkBox("School-Life", "16")
                    .checkBox("Science-Fiction", "37")
                    .checkBox("Shôjo-aï", "173")
                    .checkBox("Shônen-aï", "599")
                    .checkBox("Slice of Life", "28")
                    .checkBox("Sport", "18")
                    .checkBox("Surnaturel", "9")
                    .checkBox("Thriller", "79")
                    .checkBox("Tournois", "305")
                    .checkBox("Tragique", "10")
                    .build()

                .select("Statut", "status")
                    .addVal("Tout")
                    .add("En Cours", "ongoing")
                    .add("Terminé", "completed")
                    .add("Abandonné", "hiatus")
                    .add("En Pause", "en pause")
                    .build()

                .select("Type", "type")
                    .addVal("Tout")
                    .add("Manga", "manga")
                    .add("Manhwa", "manhwa")
                    .add("Manhua", "manhua")
                    .add("Comics", "comics")
                    .add("Novel", "novel")
                    .add("Manfra", "manfra")
                    .add("Fanfiction", "fanfiction")
                    .add("Global-manga", "global-manga")
                    .build()

                .select("Trier par", "order")
                    .addVal("Défaut")
                    .add("A-Z", "name_a-z")
                    .add("Z-A", "name_z-a")
                    .add("Mise à jour", "update")
                    .add("Date d'ajout", "added")
                    .add("Popularité", "popular")
                    .build()
                .build();
    }

    /**
     * Using text and filter at the same time doesn't work
     */
    @Override
    public List<Manga> search(String search, FilterList filter)
            throws IOException, WebsiteException, InterruptedException {
        if (filter instanceof UrlFilterList urlFilterList) {
            List<Manga> mangas = new ArrayList<>();

            if (search == null || search.isBlank()) {
                int page = 1 + filter.getOffset() / 20;
                int offset = filter.getOffset() % 20;

                while (mangas.size() < filter.getLimit()
                        && searchWithFilters(page, offset, urlFilterList, mangas)) {
                    page++;
                    offset = 0;
                }
            } else {
                int page = 1 + filter.getOffset() / 10;
                int offset = filter.getOffset() % 10;

                while (mangas.size() < filter.getLimit()
                        && searchWithText(page, offset, urlFilterList.getLimit(), search, mangas)) {
                    page++;
                    offset = 0;
                }
            }

            return mangas;
        }

        return List.of();
    }

    private boolean searchWithFilters(int page, int offset, UrlFilterList urlFilterList, List<Manga> out)
            throws IOException, InterruptedException, WebsiteException {
        HttpQueryParameterBuilder b = new HttpQueryParameterBuilder();
        b.add("page", String.valueOf(page));

        String url = urlFilterList.createRequest(url() + "/manga/", b);

        Document document = getDocument(url);
        Elements mangas = document.select(".listupd > div > div > a");

        int max = Math.min(mangas.size(), urlFilterList.getLimit() - out.size());
        for (int i = offset; i < max; i++) {
            out.add(getMangaFromSearch(mangas.get(i)));
        }

        return document.select(".hpage > .r").size() > 0;
    }

    private boolean searchWithText(int page, int offset, int limit, String search, List<Manga> out)
            throws IOException, InterruptedException, WebsiteException {
        HttpQueryParameterBuilder b = new HttpQueryParameterBuilder();
        b.add("s", Objects.requireNonNullElse(search, ""));

        String url = url() + "/page/" + page + "/?" + b.build();

        Document document = getDocument(url);
        Elements mangas = document.select(".listupd > div > div > a");

        int max = Math.min(mangas.size(), limit - out.size());
        for (int i = offset; i < max; i++) {
            out.add(getMangaFromSearch(mangas.get(i)));
        }

        return document.select(".next.page-numbers").size() > 0;
    }

    private Manga getMangaFromSearch(Element e) throws DOMException {
        Manga.Builder builder = new Manga.Builder();
        builder.setUrl(e.attr("href"));
        builder.setThumbnailURL(selectNonNull(e, "img").attr("src"));
        builder.setTitle(selectNonNull(e, ".tt").text());

        Element score = e.selectFirst(".numscore");
        if (score != null) {
            builder.setScore(Float.parseFloat(score.text()));
        }

        return builder.build();
    }
}
