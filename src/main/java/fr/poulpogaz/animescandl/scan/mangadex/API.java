package fr.poulpogaz.animescandl.scan.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.utils.HttpQueryParameterBuilder;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilterList;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.value.JsonString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class API {

    private static final ASDLLogger LOGGER = Loggers.getLogger(API.class);

    static Manga getMangaFromMangaID(String mangaID)
            throws JsonException, WebsiteException, IOException, InterruptedException {

        JsonObject object = apiJson("/manga/%s?includes[]=artist&includes[]=author&includes[]=cover_art", mangaID);

        JsonObject data = object.getAsObject("data");

        Manga.Builder builder = parseManga(data);
        builder.setScore(getScore(mangaID));

        return builder.build();
    }


    static Manga getMangaFromChapterID(String chapterID)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        Manga.Builder builder = new Manga.Builder();

        JsonObject object = apiJson(
                "/chapter/%s?includes[]=manga&includes[]=artist&includes[]=author&includes[]=cover_art",
                chapterID);

        JsonObject data = object.getAsObject("data");
        JsonArray relationships = data.getAsArray("relationships");

        // first find manga relationships
        // for getting mangaID which is necessary
        // for cover art
        for (JsonElement e : relationships) {
            if (!e.isObject()) {
                continue;
            }
            JsonObject o = (JsonObject) e;
            if (!o.getAsString("type").equals("manga")) {
                continue;
            }

            builder.setUrl(o.getAsString("id"));
            JsonObject innerAttributes = o.getAsObject("attributes");
            builder.setTitle(getEN(innerAttributes, "title"));
            builder.setDescription(getEN(innerAttributes, "description"));
            builder.setStatus(parseStatus(innerAttributes.getAsString("status")));
            parseTags(builder, innerAttributes);
            parseLanguages(builder, innerAttributes);
        }

        parseRelationships(builder, relationships);
        builder.setScore(getScore(builder.getUrl()));

        return builder.build();
    }



    static List<Chapter> getChapters(Manga manga, String language)
            throws JsonException, WebsiteException, IOException, InterruptedException {

        String mangaID = manga.getUrl();
        String request;

        if (language != null) {
            request = "/manga/%s/aggregate?translatedLanguage[]=%s".formatted(mangaID, language);
        } else {
            request = "/manga/%s/aggregate".formatted(mangaID);
        }

        JsonObject object = apiJson(request);
        JsonObject volumes = object.getAsObject("volumes");

        Chapter.Builder builder = new Chapter.Builder();
        builder.setManga(manga);

        List<Chapter> chapters = new ArrayList<>();
        for (JsonElement e : volumes.values()) {
            JsonObject volume = (JsonObject) e;
            JsonObject jsonChapters = ((JsonObject) e).getAsObject("chapters");

            String str = volume.getAsString("volume");
            float v;
            if (str.equals("none")) {
                v = Chapter.NONE_VOLUME;
            } else {
                v = Float.parseFloat(str);
            }

            builder.setVolume(v);
            for (JsonElement e2 : jsonChapters.values()) {
                JsonObject chapter = (JsonObject) e2;

                builder.setChapterNumber(chapter.getAsFloat("chapter"));
                builder.setUrl(chapter.getAsString("id"));

                chapters.add(builder.build());
            }
        }

        return chapters;
    }




    static List<String> getPages(Chapter chapter) throws JsonException, WebsiteException, IOException, InterruptedException {
        JsonObject obj = apiJson("/at-home/server/%s", chapter.getUrl());

        String baseURL = obj.getAsJsonString("baseUrl").getAsString();
        JsonObject chap = obj.getAsObject("chapter");
        String hash = chap.getAsJsonString("hash").getAsString();
        JsonArray data = chap.getAsArray("data");

        List<String> pages = new ArrayList<>();
        for (JsonElement e : data) {
            String url = e.getAsString();

            pages.add("%s/data/%s/%s".formatted(baseURL, hash, url));
        }

        return pages;
    }




    static List<Manga> search(String search, UrlFilterList list) throws JsonException, WebsiteException, IOException, InterruptedException {
        HttpQueryParameterBuilder builder = new HttpQueryParameterBuilder();

        if (search != null && !search.isEmpty()) {
            builder.add("title", search);
        }

        builder.add("limit", String.valueOf(list.getLimit()));
        builder.add("offset", String.valueOf(list.getOffset()));

        builder.add("includes[]", "cover_art");
        builder.add("includes[]", "author");
        builder.add("includes[]", "artist");

        String request = list.createRequest("/manga", builder);

        JsonArray data = apiJson(request).getAsArray("data");

        List<Manga.Builder> builders = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (JsonElement element : data) {
            if (element instanceof JsonObject object) {
                Manga.Builder b = parseManga(object);

                builders.add(b);
                ids.add(b.getUrl());
            }
        }

        if (builders.size() == 0) {
            return List.of();
        }

        Manga[] mangas = new Manga[builders.size()];

        float[] scores = getScore(ids);
        for (int i = 0; i < scores.length; i++) {
            builders.get(i).setScore(scores[i]);

            mangas[i] = builders.get(i).build();
        }

        return Arrays.asList(mangas);
    }


    static Manga.Builder parseManga(JsonObject data) {
        String mangaID = data.getAsString("id");

        JsonArray relationships = data.getAsArray("relationships");
        JsonObject attributes = data.getAsObject("attributes");

        Manga.Builder builder = new Manga.Builder();

        builder.setUrl(mangaID);
        builder.setTitle(getEN(attributes, "title"));
        builder.setDescription(getEN(attributes, "description"));
        builder.setStatus(parseStatus(attributes.getAsString("status")));
        parseTags(builder, attributes);
        parseLanguages(builder, attributes);
        parseRelationships(builder, relationships);

        return builder;
    }

    private static String getEN(JsonObject attributes, String object) {
        JsonObject title = attributes.getAsObject(object);

        return title.getAsString("en");
    }

    private static void parseRelationships(Manga.Builder builder, JsonArray relationships) {
        String mangaID = builder.getUrl();

        for (JsonElement e : relationships) {
            if (!e.isObject()) {
                continue;
            }
            JsonObject o = (JsonObject) e;

            switch (o.getAsString("type")) {
                case "author" -> {
                    String author = o.getAsObject("attributes").getAsString("name");
                    builder.setAuthor(author);
                }
                case "artist" -> {
                    String artist = o.getAsObject("attributes").getAsString("name");
                    builder.setArtist(artist);
                }
                case "cover_art" -> {
                    String filename = o.getAsObject("attributes").getAsString("fileName");
                    String url = "https://uploads.mangadex.org/covers/%s/%s"
                            .formatted(mangaID, filename);

                    builder.setThumbnailURL(url);
                }
            }
        }
    }

    private static void parseLanguages(Manga.Builder builder, JsonObject attributes) {
        for (JsonElement e : attributes.getAsArray("availableTranslatedLanguages")) {
            if (e.isNull()) {
                builder.addLanguage(null);
            } else {
                builder.addLanguage(e.getAsString());
            }
        }
    }

    private static void parseTags(Manga.Builder builder, JsonObject attributes) {
        JsonArray tags = attributes.getAsArray("tags");

        for (JsonElement e : tags) {
            if (!e.isObject()) {
                continue;
            }

            JsonObject tag = (JsonObject) e;
            String genre = tag.getAsObject("attributes")
                    .getAsObject("name")
                    .getAsString("en");

            builder.addGenre(genre);
        }
    }

    private static Status parseStatus(String status) {
        return Status.valueOf(status.toUpperCase());
    }





    static float getScore(String mangaID)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        return getScore(List.of(mangaID))[0];
    }

    static float[] getScore(List<String> mangaID)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        if (mangaID == null || mangaID.isEmpty()) {
            return new float[0];
        }

        HttpQueryParameterBuilder builder = new HttpQueryParameterBuilder();

        for (String id : mangaID) {
            builder.add("manga[]", id);
        }

        JsonObject object = apiJson("/statistics/manga?" + builder.build());
        JsonObject statistics = object.getAsObject("statistics");

        float[] scores = new float[mangaID.size()];
        for (int i = 0; i < mangaID.size(); i++) {
            String id = mangaID.get(i);

            scores[i] = statistics.getAsObject(id)
                    .getAsObject("rating")
                    .getAsFloat("average");
        }

        return scores;
    }




    private static String apiURL() {
        return "https://api.mangadex.org";
    }

    private static JsonObject apiJson(String request, String... format)
            throws WebsiteException, IOException, JsonException, InterruptedException {
        String fullRequest;

        if (format.length == 0) {
            fullRequest = apiURL() + request;
        } else {
            fullRequest = apiURL() + request.formatted((Object[]) format);
        }

        JsonElement element = HttpUtils.getJson(fullRequest);

        if (element.isObject()) {
            JsonObject object = (JsonObject) element;
            checkOk(object);

            return object;
        } else {
            throw new WebsiteException("Response to %s wasn't a json object".formatted(request));
        }
    }

    private static void checkOk(JsonObject obj) throws WebsiteException {
        JsonString str = obj.getAsJsonString("result");

        if (str == null || !str.getAsString().equals("ok")) {
            throw new WebsiteException("Failed to fetch chapter list:" + Utils.jsonString(obj));
        }
    }
}
