package fr.poulpogaz.animescandl.website.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.value.JsonString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class API {

    static Manga getMangaFromMangaID(String mangaID)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        Manga.Builder builder = new Manga.Builder();

        JsonObject object = apiJson("/manga/%s?includes[]=artist&includes[]=author&includes[]=cover_art", mangaID);
        JsonObject data = object.getAsObject("data");
        JsonObject attributes = data.getAsObject("attributes");
        JsonArray relationships = data.getAsArray("relationships");

        builder.setUrl(mangaID);
        builder.setTitle(getEN(attributes, "title"));
        builder.setDescription(getEN(attributes, "description"));
        builder.setStatus(parseStatus(attributes.getAsString("status")));
        parseTags(builder, attributes);
        parseLanguages(builder, attributes);
        parseRelationships(builder, relationships);
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
            if (!e.isObject() || ((JsonObject) e).getAsString("type").equals("manga")) {
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

        JsonObject object = apiJson("/statistics/manga/%s", mangaID);

        return object.getAsObject("statistics")
                .getAsObject(mangaID)
                .getAsObject("rating")
                .getAsFloat("average");
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

        List<Chapter> chapters = new ArrayList<>();
        for (JsonElement e : volumes.values()) {
            JsonObject volume = (JsonObject) e;
            JsonObject jsonChapters = ((JsonObject) e).getAsObject("chapters");

            String str = volume.getAsString("volume");
            float v;
            if (str.equals("none")) {
                v = Chapter.NONE_CHAPTER;
            } else {
                v = Float.parseFloat(str);
            }

            for (JsonElement e2 : jsonChapters.values()) {
                JsonObject chapter = (JsonObject) e2;

                float chapterNumber = chapter.getAsFloat("chapter");
                String chapterID = chapter.getAsString("id");

                chapters.add(new Chapter(chapterID, chapterNumber, null, v));
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






    private static String apiURL() {
        return "https://api.mangadex.org";
    }

    private static JsonObject apiJson(String request, String... format)
            throws WebsiteException, IOException, JsonException, InterruptedException {
        JsonElement element = HttpUtils.getJson(apiURL() + request.formatted((Object[]) format));

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
