package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.*;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.DOMException;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.*;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Nekosama extends AbstractWebsite implements AnimeWebsite, SearchWebsite<Anime> {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Nekosama.class);

    private static final Pattern PSTREAM_REGEX = Pattern.compile("'(https://www\\.pstream\\.net.*?)'");
    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("e\\.parseJSON\\(atob\\(t\\)\\.slice\\(2\\)\\)}\\(\"(.*?)\"");
    // 27.12.2021 old version= "e\.parseJSON\(atob\(t\)\)}\("(.*?)""
    // e\.parseJSON\(atob\(t\)\.slice\(1\)\)}\("(.*?)


    private static final String SEARCH_URL = "/animes-search-vostfr.json?203457a80220e5bc1408b774c035099d";
    private static List<Anime> ANIME_SEARCH;

    private static final String ACTION        = "Action";
    private static final String ADVENTURE     = "Aventure";
    private static final String COMEDY        = "Comédie";
    private static final String DRAMA         = "Drama";
    private static final String ECCHI         = "Ecchi";
    private static final String FANTASY       = "Fantastique";
    private static final String HENTAI        = "Hentai";
    private static final String HORROR        = "Horreur";
    private static final String MAGICAL_GIRL  = "Magical Girl";
    private static final String MECHA         = "Mecha";
    private static final String MUSIC         = "Music";
    private static final String MYSTERY       = "Mystery";
    private static final String PSYCHOLOGICAL = "Psychologique";
    private static final String ROMANCE       = "Romance";
    private static final String SCI_FI        = "Sci-Fi";
    private static final String SLICE_OF_LIFE = "Tranche de vie";
    private static final String SPORTS        = "Sports";
    private static final String SUPERNATURAL  = "Surnaturel";
    private static final String THRILLER      = "Suspense";

    @Override
    public String name() {
        return "Nekosama";
    }

    @Override
    public String url() {
        return "https://neko-sama.fr";
    }

    @Override
    public String version() {
        return "dev1";
    }

    @Override
    public boolean isEpisodeURL(String url) {
        return url.contains("neko-sama.fr/anime/episode/");
    }

    @Override
    public boolean isAnimeURL(String url) {
        return url.contains("neko-sama.fr/anime/info/");
    }

    @Override
    public HttpHeaders standardHeaders() {
        return super.standardHeaders()
                .setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader("accept-language", "en-US,en;q=0.5");
    }

    @Override
    public Anime getAnime(String url) throws IOException, InterruptedException, WebsiteException, JsonException {
        String animeURL = getAnimeURL(url);
        Document doc = getDocument(animeURL);

        Anime.Builder builder = new Anime.Builder();
        builder.setUrl(animeURL);
        setTitle(doc, builder);
        setGenres(doc, builder);

        Element description = selectNonNull(doc, ".synopsis > p");
        builder.setDescription(description.text());

        Element thumbnail = selectNonNull(doc, ".cover > img");
        builder.setThumbnailURL(thumbnail.attr("src"));

        Element animeInfo = getElementById(doc, "anime-info-list");
        for (Element child : animeInfo.children()) {
            Element small = selectNonNull(child, "small");

            switch (small.text()) {
                case "Score moyen" -> {
                    String s = Utils.getFirstNonEmptyText(child, true).replace("/5", "");

                    builder.setScore(Float.parseFloat(s));
                }
                case "Status" -> {
                    String s = Utils.getFirstNonEmptyText(child, true);

                    builder.setStatus(parseStatus(s));
                }
                case "Episodes" -> {
                    String s = Utils.getFirstNonEmptyText(child, true);

                    builder.setNEpisode(Integer.parseInt(s));
                }
                case "Format" -> {
                    String s = Utils.getFirstNonEmptyText(child, true);

                    builder.setType(parseType(s));
                }
            }
        }

        return builder.build();
    }

    protected String getAnimeURL(String url) throws IOException, InterruptedException, WebsiteException {
        if (isAnimeURL(url)) {
            return url;
        } else if (isEpisodeURL(url)) {
            Document document = getDocument(url);
            Element a = selectNonNull(document, ".details > .info > h1 > a");

            return url() + a.attr("href");
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }
    }

    protected void setTitle(Document doc, Anime.Builder builder) throws DOMException {
        Element title = selectNonNull(doc, ".offset-lg-3 > h1");
        List<TextNode> nodes = title.textNodes();

        if (nodes.size() > 0) {
            String t = nodes.get(0).text();
            if (t.endsWith(" VOSTFR ")) {
                builder.setTitle(t.substring(0, t.length() - 8));

            } else if (t.endsWith(" VF ")) {
                builder.setTitle(t.substring(0, t.length() - 4));

            } else {
                builder.setTitle(t);
            }

        } else {
            throw new DOMException("Can't find title");
        }
    }

    protected void setGenres(Document doc, Anime.Builder builder) {
        Elements elements = doc.select(".ui.tag > a");

        for (Element e : elements) {
            builder.addGenre(e.text());
        }
    }

    protected Status parseStatus(String s) {
        return switch (s) {
            case "Terminé", "2" -> Status.COMPLETED;
            case "En cours", "Pas encore commencé", "1" -> Status.ONGOING;
            default -> null;
        };
    }

    protected Type parseType(String s) {
        return switch (s) {
            case "movie", "m0v1e" -> Type.MOVIE;
            case "tv" -> Type.TV;
            case "ova" -> Type.OVA;
            case "special" -> Type.SPECIAL;
            default -> null;
        };
    }

    @Override
    public List<Episode> getEpisodes(Anime anime)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        Document doc = getDocument(anime.getUrl());

        List<Episode> episodes = new ArrayList<>();
        Episode.Builder builder = new Episode.Builder();
        builder.setAnime(anime);

        Element element = selectNonNull(doc, "div#main > script:first-of-type");

        String js = element.html();
        String episodeList = Utils.getRegexGroup(js, "var episodes = (\\[.*\\])");

        JsonArray array = (JsonArray) JsonTreeReader.read(new StringReader(episodeList));
        for (JsonElement e : array) {
            JsonObject o = (JsonObject) e;

            builder.setUrl(url() + o.getAsString("url"));

            String epStr = o.getAsString("episode");
            builder.setEpisode(Utils.getFirstInt(epStr));

            episodes.add(builder.build());
        }

        return episodes;
    }

    /**
     * Find the pstream url
     */
    @Override
    public List<Source> getSources(Episode episode) throws IOException, InterruptedException, JsonException, WebsiteException {
        Document document = getDocument(episode.getUrl());

        Matcher matcher = PSTREAM_REGEX.matcher(document.outerHtml());

        if (matcher.find()) {
            return getSources(matcher.group(1));
        }

        return null;
    }

    /**
     * Find js in pstream
     * Find base 64, decode and parse json in js
     */
    protected List<Source> getSources(String pstreamURL) throws IOException, InterruptedException, WebsiteException, JsonException {
        Document doc = getDocument(pstreamURL);

        Element subtitleElement = doc.selectFirst("track[kind=subtitles]");
        String subtitles = null;
        if (subtitleElement != null) {
            subtitles = subtitleElement.attr("src");
        }

        Element jsURL = selectNonNull(doc, "script[src^=https://www.pstream.net/u/player-script]");

        String js = jsURL.attr("src");
        InputStream is = getInputStream(js);
        if (is == null) {
            throw new WebsiteException("Failed to extract video");
        }

        String content = new String(is.readAllBytes());
        Matcher matcher = VIDEO_LINK_REGEX.matcher(content);
        if (matcher.find()) {
            String json = decode(matcher.group(1)).substring(2);
            String videoURL = getURL(json);

            return readM3U8(subtitles, videoURL);
        } else {
            return null;
        }
    }

    private String decode(String base64) {
        byte[] out = Base64.getDecoder().decode(base64);
        return new String(out);
    }

    private String getURL(String json) throws JsonException, IOException {
        JsonObject object = (JsonObject) JsonTreeReader.read(new StringReader(json));

        // LOL
        return object.getAsJsonString("mmmmmmmmmmmmmmmmmmmm").getAsString();
    }

    /**
     * Parse m3u8
     */
    protected List<Source> readM3U8(String subtitle, String m3U8URL) throws IOException, InterruptedException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(getInputStream(m3U8URL)));

        List<Source> sources = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                String group = Utils.getRegexGroup(line, "RESOLUTION=\\d+x(\\d+)");

                int height = Integer.parseInt(group);

                String url = br.readLine();
                sources.add(new Source(url, subtitle, height, M3U8Downloader.FORMAT));
            }
        }
        br.close();

        return sources;
    }

    @Override
    public FilterList getSearchFilter() {
        return new FilterList.Builder()
                .addFilter(createSortFilter())
                .addFilter(createStatusFilter())
                .addFilter(createTypeFilter())
                .addFilter(createGenreFilter())
                .build();
    }

    private Filter<?> createTypeFilter() {
        return new NekosamaSelect("Format",
                List.of("Tous", "Film", "OAV", "Special", "TV")) {

            private static final Type[] TYPES = new Type[] {Type.MOVIE, Type.OVA, Type.SPECIAL, Type.TV};

            @Override
            public Stream<Anime> filter(Stream<Anime> stream) {
                if (value == null || value == 0) {
                    return stream;
                }

                Type expected = TYPES[value - 1];

                return stream.filter((a) -> {
                    Type current = a.getType().orElse(null);

                    return current != null && current == expected;
                });
            }
        };
    }

    private Filter<?> createStatusFilter() {
        return new NekosamaSelect("Status",
                List.of("Tous", "Terminé", "En cours")) {

            @Override
            public Stream<Anime> filter(Stream<Anime> stream) {
                if (value == null || value == 0) {
                    return stream;
                }

                Status expected = value == 1 ? Status.COMPLETED : Status.ONGOING;

                return stream.filter((a) -> {
                    Status current = a.getStatus().orElse(null);

                    return current != null && current == expected;
                });
            }
        };
    }

    private Filter<?> createSortFilter() {
        return new NekosamaSelect("Ordre",
                List.of("Aucun", "Score (+ AU -)", "Score (- AU +)", "Titre (A-Z)", "Titre (Z-A)")) {

            @Override
            public Stream<Anime> filter(Stream<Anime> stream) {
                if (value == null || value == 0) {
                    return stream;
                }

                return switch (value) {
                    case 1 -> stream.sorted((a, b) -> compareScore(a, b));
                    case 2 -> stream.sorted((a, b) -> compareScore(b, a));
                    case 3 -> stream.sorted(Comparator.comparing(Anime::getTitle));
                    case 4 -> stream.sorted(Comparator.comparing(Anime::getTitle).reversed());
                    default -> stream;
                };
            }
        };
    }

    private int compareScore(Anime a, Anime b) {
        Optional<Float> scoreA = a.getScore();
        Optional<Float> scoreB = b.getScore();

        if (scoreA.isPresent() && scoreB.isPresent()) {
            return Float.compare(scoreA.get(), scoreB.get());
        } else if (scoreA.isPresent()) {
            return 1;
        } else if (scoreB.isPresent()) {
            return -1;
        } else {
            return 0;
        }
    }

    private Filter<?> createGenreFilter() {
        List<Filter<?>> filters = new ArrayList<>();
        filters.add(new GenreCheckBox(ACTION));
        filters.add(new GenreCheckBox(ADVENTURE));
        filters.add(new GenreCheckBox(COMEDY));
        filters.add(new GenreCheckBox(DRAMA));
        filters.add(new GenreCheckBox(ECCHI));
        filters.add(new GenreCheckBox(FANTASY));
        filters.add(new GenreCheckBox(HENTAI));
        filters.add(new GenreCheckBox(HORROR));
        filters.add(new GenreCheckBox(MAGICAL_GIRL));
        filters.add(new GenreCheckBox(MECHA));
        filters.add(new GenreCheckBox(MUSIC));
        filters.add(new GenreCheckBox(MYSTERY));
        filters.add(new GenreCheckBox(PSYCHOLOGICAL));
        filters.add(new GenreCheckBox(ROMANCE));
        filters.add(new GenreCheckBox(SCI_FI));
        filters.add(new GenreCheckBox(SLICE_OF_LIFE));
        filters.add(new GenreCheckBox(SPORTS));
        filters.add(new GenreCheckBox(SUPERNATURAL));
        filters.add(new GenreCheckBox(THRILLER));

        return new NekosamaGroup("Genres", filters);
    }

    @Override
    public List<Anime> search(String search, FilterList filterList) throws JsonException, IOException, InterruptedException {
        loadAnimeSearchJson();

        // sort after filtering for best performance
        NekosamaFilter sortFilter = null;

        Stream<Anime> animeStream = ANIME_SEARCH.stream();
        for (Filter<?> filter : filterList.getFilters()) {
            if (filter instanceof NekosamaFilter nFilter) {
                if (filter.getName().equals("Ordre")) {
                    sortFilter = nFilter;
                } else {
                    animeStream = nFilter.filter(animeStream);
                }
            }
        }

        if (sortFilter != null) {
            animeStream = sortFilter.filter(animeStream);
        }

        animeStream = animeStream.dropWhile(new Predicate<>() {
            private int index = 0;

            @Override
            public boolean test(Anime anime) {
                return index++ < filterList.getOffset();
            }
        });

        return animeStream.limit(filterList.getLimit()).toList();
    }

    private void loadAnimeSearchJson() throws JsonException, IOException, InterruptedException {
        if (ANIME_SEARCH != null) {
            return;
        }

        List<Anime> animes = new ArrayList<>();

        JsonArray array = (JsonArray) getJson(url() + SEARCH_URL);
        for (JsonElement e : array) {
            JsonObject object = (JsonObject) e;

            Anime.Builder builder = new Anime.Builder();
            builder.setTitle(object.getAsString("title"));
            builder.setType(parseType(object.getAsString("type")));
            builder.setUrl(url() + object.getAsString("url"));

            JsonArray genres = object.getAsArray("genres");
            for (JsonElement g : genres) {
                builder.addGenre(parseGenre(g.getAsString()));
            }

            builder.setStatus(parseStatus(object.getAsString("status")));
            builder.setThumbnailURL(object.getAsString("url_image"));
            builder.setScore(Utils.parseFloat(object.getAsString("score")).orElse(-1f));

            if (builder.getType() != Type.MOVIE) {
                builder.setNEpisode(Utils.getFirstInteger(object.getAsString("nb_eps")).orElse(-1));
            } else {
                builder.setNEpisode(1);
            }

            animes.add(builder.build());
        }

        ANIME_SEARCH = animes;
    }

    private String parseGenre(String genre) {
        return switch (genre) {
            case "action"        -> ACTION;
            case "adventure"     -> ADVENTURE;
            case "c0m1dy"        -> COMEDY;
            case "drama"         -> DRAMA;
            case "ecchi"         -> ECCHI;
            case "fantasy"       -> FANTASY;
            case "hentai"        -> HENTAI;
            case "horror"        -> HORROR;
            case "mahou shoujo"  -> MAGICAL_GIRL;
            case "mecha"         -> MECHA;
            case "music"         -> MUSIC;
            case "mystery"       -> MYSTERY;
            case "psychological" -> PSYCHOLOGICAL;
            case "romance"       -> ROMANCE;
            case "sci-fi"        -> SCI_FI;
            case "slice of life" -> SLICE_OF_LIFE;
            case "sports"        -> SPORTS;
            case "supernatural"  -> SUPERNATURAL;
            case "thriller"      -> THRILLER;
            default -> null;
        };
    }

    private static class GenreCheckBox extends CheckBox implements NekosamaFilter {

        public GenreCheckBox(String genre) {
            super(genre);
        }

        @Override
        public Stream<Anime> filter(Stream<Anime> stream) {
            return stream.filter((a) -> {
                if (isSelected()) {
                    return a.getGenres().contains(getName());
                } else {
                    return true;
                }
            });
        }
    }

    private static abstract class NekosamaSelect extends Select<String> implements NekosamaFilter {

        public NekosamaSelect(String name, List<String> acceptedValues) {
            super(name, acceptedValues);
        }
    }

    private static class NekosamaGroup extends Group implements NekosamaFilter {

        public NekosamaGroup(String name, List<Filter<?>> filters) {
            super(name, filters);
        }

        @Override
        public Stream<Anime> filter(Stream<Anime> stream) {
            Stream<Anime> s = stream;
            for (Filter<?> filter : getFilters()) {
                if (filter instanceof NekosamaFilter nfilter) {
                    s = nfilter.filter(s);
                }
            }

            return s;
        }
    }

    private interface NekosamaFilter {

        Stream<Anime> filter(Stream<Anime> stream);
    }
}
