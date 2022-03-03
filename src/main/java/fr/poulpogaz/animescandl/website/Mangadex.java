package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.DefaultTitle;
import fr.poulpogaz.animescandl.utils.AbstractScanWriter;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonValue;
import fr.poulpogaz.json.tree.value.JsonNumber;
import fr.poulpogaz.json.tree.value.JsonString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <a href="https://api.mangadex.org/docs.html">Mangadex doc</a>
 *
 * @version 27.02.2022
 */
public class Mangadex extends AbstractWebsite<Mangadex.MangadexChapter, Mangadex.MangadexTitle> {

    public static final Mangadex INSTANCE = new Mangadex();

    private static final Logger LOGGER = LogManager.getLogger(Mangadex.class);

    private AbstractScanWriter sw;

    private Mangadex() {
        super();
    }

    protected JsonObject apiJson(String request, String... format) throws WebsiteException, IOException, JsonException, InterruptedException {
        JsonElement element = getJson(apiURL() + request.formatted((Object[]) format));

        if (element.isObject()) {
            JsonObject object = (JsonObject) element;
            checkOk(object);

            return object;
        } else {
            throw new WebsiteException("Response to %s wasn't a json object".formatted(request));
        }
    }

    protected void checkOk(JsonObject obj) throws WebsiteException {
        JsonString str = obj.getAsJsonString("result");

        if (str == null || !str.getAsString().equals("ok")) {
            throw new WebsiteException("Failed to fetch chapter list:" + Utils.jsonString(obj));
        }
    }

    @Override
    protected List<MangadexChapter> fetchList(String url, Settings settings) throws Throwable {
        if (settings.language() == null) {
            throw new WebsiteException("Please set language");
        }

        if (settings.range() == null && isChapterPage(url)) {
            return single(url, settings);
        } else {
            return multiple(url, settings);
        }
    }

    protected List<MangadexChapter> single(String url, Settings settings) throws JsonException, WebsiteException, IOException, InterruptedException {
        String chapterID = Utils.getRegexGroup(url, "chapter/(.*)/");

        JsonObject object = apiJson("/chapter/%s?includes[]=manga", chapterID);
        String chapter = object.getAsObject("data")
                .getAsObject("attributes")
                .getAsJsonString("chapter")
                .getAsString();
        String title = null;

        JsonArray relationships = object.getAsObject("data").getAsArray("relationships");
        for (JsonElement e : relationships) {
            JsonObject relation = (JsonObject) e;

            if (relation.getAsJsonString("type").getAsString().equals("manga")) {
                title = relation.getAsObject("attributes")
                        .getAsObject("title")
                        .getAsJsonString(settings.language())
                        .getAsString();
            }
        }

        return List.of(new MangadexChapter(chapterID, 0, chapter, title));
    }

    protected List<MangadexChapter> multiple(String url, Settings settings) throws JsonException, WebsiteException, IOException, InterruptedException {
        List<MangadexChapter> entries = new ArrayList<>();

        String mangaID = getMangaID(url);
        String manga = getMangaName(mangaID);

        JsonObject obj = apiJson("/manga/%s/aggregate?translatedLanguage[]=%s", mangaID, settings.language());
        JsonObject volumes = obj.getAsObject("volumes");

        for (JsonElement e : volumes.values()) {
            JsonObject chapters = ((JsonObject) e).getAsObject("chapters");

            for (JsonElement e2 : chapters.values()) {
                JsonObject chapter = (JsonObject) e2;

                String c = chapter.getAsJsonString("chapter").getAsString();
                String chapterID = chapter.getAsJsonString("id").getAsString();
                int index = Utils.getFirstInt(c);

                entries.add(new MangadexChapter(chapterID, index, c, manga));
            }
        }

        entries.sort(Utils::chapterComparator);

        return entries;
    }

    protected String getMangaID(String url) throws JsonException, WebsiteException, IOException, InterruptedException {
        if (isMangaPage(url)) {
             return Utils.getRegexGroup(url, "title/(.*)/?$");

        } else if (isChapterPage(url)) {
            String chapterID = Utils.getRegexGroup(url, "chapter/(.*)/?$");

            JsonObject object = apiJson("/chapter/%s?includes[]=manga", chapterID);
            JsonArray array = object.getAsObject("data").getAsArray("relationships");

            String id = null;
            for (JsonElement e : array) {
                JsonObject o = (JsonObject) e;

                if (o.getAsJsonString("type").getAsString().equals("manga")) {
                    id = o.getAsJsonString("id").getAsString();
                    break;
                }
            }

            if (id == null) {
                throw new WebsiteException("Failed to get manga id");
            }

            return id;
        } else {
            throw new WebsiteException("Unsupported url: " + url);
        }
    }

    private String getMangaName(String id) throws JsonException, WebsiteException, IOException, InterruptedException {
        JsonObject object = apiJson("/manga/%s", id);
        JsonObject titleObj = object.getAsObject("data").getAsObject("attributes").getAsObject("title");

        return titleObj.getAsJsonString("en").getAsString();
    }

    private boolean isMangaPage(String url) {
        return url.startsWith("https://mangadex.org/title/");
    }

    private boolean isChapterPage(String url) {
        return url.startsWith("https://mangadex.org/chapter/");
    }

    @Override
    protected Path getOutputFile(MangadexChapter entry, Settings settings) {
        String fileName = entry.manga() + " - " + entry.chapter() + ".pdf";

        if (settings.out() != null) {
            return settings.out().resolve(fileName);
        } else {
            return Path.of(fileName);
        }
    }

    @Override
    protected boolean preDownload(List<MangadexChapter> entries, Settings settings) throws Throwable {
        if (entries.size() == 1) {
            sw = AbstractScanWriter.newWriter(null, false, settings.out());
        } else {
            sw = AbstractScanWriter.newWriter(entries.get(0).manga(), settings.concatenateAll(), settings.out());
        }

        return !Main.noOverwrites.isPresent() || !settings.concatenateAll() || !Files.exists(sw.allPath());
    }

    @Override
    protected void postDownload(List<MangadexChapter> entries, Settings settings) throws Throwable {
        sw.endAll();
        sw = null;
    }

    @Override
    protected void processEntry(MangadexChapter entry, Settings settings) throws Throwable {
        LOGGER.info("Downloading {} - {}", entry.manga(), entry.chapter());

        JsonObject obj = apiJson("/at-home/server/%s", entry.id());

        String baseURL = obj.getAsJsonString("baseUrl").getAsString();
        JsonObject chapter = obj.getAsObject("chapter");
        String hash = chapter.getAsJsonString("hash").getAsString();
        JsonArray data = chapter.getAsArray("data");

        List<String> pages = new ArrayList<>();
        for (JsonElement e : data) {
            String url = ((JsonString) e).getAsString();

            pages.add("%s/data/%s/%s".formatted(baseURL, hash, url));
        }

        sw.newScan(entry.manga() + " - " + entry.chapter());
        downloadPDF(sw, pages);
        sw.endScan();
    }

    @Override
    public String name() {
        return "Mangadex";
    }

    @Override
    public String version() {
        return "27.02.2022";
    }

    protected String apiURL() {
        return "https://api.mangadex.org";
    }

    @Override
    public String url() {
        return "https://mangadex.org";
    }

    @Override
    public List<MangadexTitle> search(String search, Settings settings) throws Throwable {
        // first search
        JsonObject root;
        if (settings.language() != null) {
            root = apiJson("/manga?limit=100&title=%s&availableTranslatedLanguage=%s", search, settings.language());
        } else {
            root = apiJson("/manga?limit=100&title=%s", search);
        }

        Map<String, MTBuilder> builders = createBuilders(root);
        if (builders.isEmpty()) {
            return List.of();
        }

        // send stats request
        getStats(builders);

        // finally, create MangadexTitle
        List<MangadexTitle> titles = new ArrayList<>();
        for (MTBuilder builder : builders.values()) {
            List<MangadexTitle> t = builder.build();
            t.sort(Comparator.comparing(MangadexTitle::score));

            titles.addAll(t);
        }

        return Collections.unmodifiableList(titles);
    }

    protected Map<String, MTBuilder> createBuilders(JsonObject root) {
        JsonArray data = root.getAsArray("data");
        if (data.size() == 0) {
            return Map.of();
        }

        Map<String, MTBuilder> builders = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            JsonObject o = data.getAsObject(i);
            JsonObject attributes = o.getAsObject("attributes");

            String id = o.getAsJsonString("id").getAsString();

            // ugly code
            JsonObject titleObj = attributes.getAsObject("title");
            String title;
            if (titleObj.get("en") == null) {
                if (titleObj.get("ja") == null) {
                    JsonElement e = titleObj.values().iterator().next();

                    title = ((JsonValue) e).getAsString();
                } else {
                    title = titleObj.getAsJsonString("ja").getAsString();
                }
            } else {
                title = titleObj.getAsJsonString("en").getAsString();
            }

            MTBuilder builder = new MTBuilder();
            builder.setName(title).setUrl(url() + "/title/" + id).setWebsite(this);

            for (JsonElement e : attributes.getAsArray("availableTranslatedLanguages")) {
                builder.addLanguage(((JsonValue) e).getAsString());
            }

            builders.put(id, builder);
        }

        return builders;
    }

    protected void getStats(Map<String, MTBuilder> builders) throws JsonException, WebsiteException, IOException, InterruptedException {
        // create request
        StringBuilder request = new StringBuilder();
        request.append("/statistics/manga?");

        int i = 0;
        for (String id : builders.keySet()) {
            request.append("manga[]=").append(id);
            if (i + 1 < builders.size()) {
                request.append("&");
            }
            i++;
        }

        // send request
        JsonObject root = apiJson(request.toString());
        JsonObject statistics = root.getAsObject("statistics");

        // set scores
        for (Map.Entry<String, JsonElement> e : statistics.entrySet()) {
            MTBuilder builder = builders.get(e.getKey());

            JsonElement average = ((JsonObject) e.getValue()).getAsObject("rating").get("average");

            float rating = 0;
            if (average instanceof JsonNumber) {
                rating = ((JsonNumber) average).getAsFloat();
            }

            builder.setScore(rating);
        }
    }

    protected static class MangadexChapter extends Chapter {

        private final String manga;

        public MangadexChapter(String id, int index, String chapter, String manga) {
            super(id, index, chapter);
            this.manga = manga;
        }

        public String id() {
            return url();
        }

        public String manga() {
            return manga;
        }

        @Override
        public String toString() {
            return "MangadexChapter{" +
                    "url='" + url + '\'' +
                    ", index=" + index +
                    ", chapter='" + chapter + '\'' +
                    ", manga='" + manga + '\'' +
                    '}';
        }
    }

    protected static class MangadexTitle extends DefaultTitle {

        private final String language;
        private final float score;

        public MangadexTitle(String url, String name, String language, float score, Website<?> website) {
            super(url, name, website);
            this.language = language;
            this.score = score;
        }

        public String language() {
            return language;
        }

        public float score() {
            return score;
        }

        @Override
        public Settings createSettings(Settings old) {
            return new Settings(url, old.range(), old.concatenateAll(), language, old.out());
        }

        @Override
        public String toString() {
            return "%s, %f☆ (%s)".formatted(name, score, language);
        }
    }

    protected static class MTBuilder {

        private String url;
        private String name;
        private List<String> languages;
        private float score;
        private Website<?> website;

        public MTBuilder() {
            languages = new ArrayList<>();
        }

        public List<MangadexTitle> build() {
            return languages.stream()
                    .map((l) -> new MangadexTitle(url, name, l, score, website))
                    .collect(Collectors.toList());
        }

        public String getUrl() {
            return url;
        }

        public MTBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getName() {
            return name;
        }

        public MTBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public MTBuilder addLanguage(String language) {
            languages.add(language);
            return this;
        }

        public List<String> getLanguages() {
            return languages;
        }

        public MTBuilder setLanguage(List<String> languages) {
            this.languages = languages;
            return this;
        }

        public float getScore() {
            return score;
        }

        public MTBuilder setScore(float score) {
            this.score = score;
            return this;
        }

        public Website<?> getWebsite() {
            return website;
        }

        public MTBuilder setWebsite(Website<?> website) {
            this.website = website;
            return this;
        }
    }
}
