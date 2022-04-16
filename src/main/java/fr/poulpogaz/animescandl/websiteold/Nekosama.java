package fr.poulpogaz.animescandl.websiteold;

import fr.poulpogaz.animescandl.Video;
import fr.poulpogaz.animescandl.extractors.IExtractor;
import fr.poulpogaz.animescandl.extractors.PStreamExtractor;
import fr.poulpogaz.animescandl.modelold.DefaultEntry;
import fr.poulpogaz.animescandl.modelold.DefaultTitle;
import fr.poulpogaz.animescandl.utils.ExtractorException;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonReader;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 27.02.2022
 */
public class Nekosama extends AbstractVideoWebsite<DefaultEntry, Nekosama.Anime> {

    public static final Nekosama INSTANCE = new Nekosama();

    private static final Logger LOGGER = LogManager.getLogger(Nekosama.class);

    private static final String BASE_URL = "https://neko-sama.fr";

    private static final IExtractor[] EXTRACTORS = new IExtractor[] {new PStreamExtractor()};
    private static final Pattern PSTREAM_REGEX = Pattern.compile("'(https://www\\.pstream\\.net.*?)'");

    private static final String GET_ANIMES_JSON = "https://neko-sama.fr/animes-search-vostfr.json?4d32e639d1c99f50aad380134a022a78";
    private static List<Anime> ANIMES;

    private Nekosama() {
        super();
    }

    // https://neko-sama.fr/anime/episode/3458-hagane-no-renkinjutsushi-fullmetal-alchemist-01-vostfr
    @Override
    public String getFileName(String url) {
        return Utils.getRegexGroup(url, "neko-sama\\.fr/anime/episode/\\d*-(.*?)/?$");
    }

    @Override
    public HttpHeaders standardHeaders() {
        return super.standardHeaders()
                .setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader("accept-language", "en-US,en;q=0.5");
    }

    @Override
    public String name() {
        return "Neko-sama";
    }

    @Override
    public String version() {
        return "27.02.2022";
    }

    @Override
    public String url() {
        return BASE_URL;
    }

    @Override
    protected List<DefaultEntry> fetchList(String url, Settings settings) throws IOException, WebsiteException, InterruptedException, JsonException {
        if (settings.range() == null && isEpisode(url)) {
            return List.of(new DefaultEntry(url, 0));
        } else {
            String animeInfoURL = getAnimeInfoURL(url);

            Document document = getDocument(animeInfoURL);
            Element element = document.selectFirst("div#main > script:first-of-type");

            if (element == null) {
                throw new WebsiteException("Failed to fetch videos");
            }

            String js = element.html();
            String episodeList = Utils.getRegexGroup(js, "var episodes = (\\[.*\\])");

            JsonArray array = (JsonArray) JsonTreeReader.read(new StringReader(episodeList));
            List<DefaultEntry> episodes = new ArrayList<>();
            for (JsonElement e : array) {
                JsonObject o = (JsonObject) e;

                String epURL = o.getAsJsonString("url").getAsString();
                String epStr = o.getAsJsonString("episode").getAsString();
                int ep = Utils.getFirstInt(epStr);

                episodes.add(new DefaultEntry(BASE_URL + epURL, ep));
            }

            return episodes;
        }
    }

    protected String getAnimeInfoURL(String url) throws IOException, InterruptedException, WebsiteException {
        if (isAnimeInfoURL(url)) {
            return url;
        } else if (isEpisode(url)) {
            Document document = getDocument(url);
            Element a = document.selectFirst("details > info > h1 > a");

            if (a == null) {
                throw new WebsiteException("Failed to get anime info url");
            }

            return BASE_URL + a.attr("href");
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }
    }

    protected boolean isAnimeInfoURL(String url) {
        return url.contains("neko-sama.fr/anime/info/");
    }

    protected boolean isEpisode(String url) {
        return url.contains("neko-sama.fr/anime/episode/");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public List<Video> extract(String url) throws ExtractorException, IOException, InterruptedException {
        LOGGER.info("Extracting video(s) at neko-sama.fr");

        Document document = getDocument(url);

        List<Video> allVideos = new ArrayList<>();

        for (IExtractor extractor : EXTRACTORS) {
            Element element = document.selectFirst("iframe[src*=%s]".formatted(extractor.url()));

            if (element != null) {
                String videoURL = element.attr("src");

                List<Video> videos = extractor.extract(this, videoURL);

                if (videos == null) {
                    continue;
                }

                allVideos.addAll(videos);
            }

            if (extractor instanceof PStreamExtractor) {
                Matcher matcher = PSTREAM_REGEX.matcher(document.outerHtml());

                if (matcher.find()) {
                    allVideos.addAll(extractor.extract(this, matcher.group(1)));
                }
            }
        }

        return allVideos;
    }

    // **********
    // * SEARCH *
    // **********

    @Override
    public List<Anime> search(String search, Settings settings) throws Throwable {
        if (ANIMES == null) {
            initSearch();
        }

        search = search.toLowerCase(Locale.ROOT);

        List<Anime> results = new ArrayList<>();

        for (Anime anime : ANIMES) {
            String name = anime.name().toLowerCase(Locale.ROOT);
            String others = anime.others().toLowerCase(Locale.ROOT);

            if (name.contains(search) || others.contains(search)) {
                results.add(anime);
            }
        }

        results.sort(Comparator.comparingDouble(Anime::score).reversed());

        return Collections.unmodifiableList(results);
    }

    private void initSearch() throws IOException, InterruptedException, JsonException {
        ANIMES = new ArrayList<>();

        InputStream is = getInputStream(GET_ANIMES_JSON);

        if (is == null) {
            LOGGER.warn("Failed to retrieve nekosama entries (is == null)");
            return;
        }

        JsonReader reader = new JsonReader(is);
        reader.beginArray();
        while (!reader.isArrayEnd()) {
            reader.beginObject();

            ANIMES.add(parseEntry(reader));

            reader.endObject();
        }
        reader.endArray();
        reader.close();
    }

    private static Anime parseEntry(JsonReader jr) throws JsonException, IOException {
        String title = null;
        String url = null;
        String others = null;
        float score = 0;

        while (!jr.isObjectEnd()) {
            switch (jr.nextKey()) {
                case "title" -> {
                    title = jr.nextString();
                }
                case "url" -> {
                    url = BASE_URL + jr.nextString();
                }
                case "others" -> {
                    others = jr.nextString();
                }
                case "score" -> {
                    score = Utils.parseFloat(jr.nextString()).orElse(0f);
                }
                default -> jr.skipValue();
            }
        }

        return new Anime(url, title, others, score);
    }

    protected static class Anime extends DefaultTitle {

        private final String others;
        private final float score;

        private Anime(String url, String name, String others, float score) {
            super(url, name, Nekosama.INSTANCE);
            this.others = others;
            this.score = score;
        }

        public String others() {
            return others;
        }

        public float score() {
            return score;
        }

        @Override
        public String toString() {
            return "%s (%s☆)".formatted(name, score);
        }
    }
}