package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.model.Episode;
import fr.poulpogaz.animescandl.model.Source;
import fr.poulpogaz.animescandl.model.Status;
import fr.poulpogaz.animescandl.utils.HttpHeaders;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.DOMException;
import fr.poulpogaz.animescandl.website.WebsiteException;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nekosama extends AbstractWebsite implements AnimeWebsite<Anime, Episode> {

    private static final Pattern PSTREAM_REGEX = Pattern.compile("'(https://www\\.pstream\\.net.*?)'");
    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("e\\.parseJSON\\(atob\\(t\\)\\.slice\\(2\\)\\)}\\(\"(.*?)\"");
    // 27.12.2021 old version= "e\.parseJSON\(atob\(t\)\)}\("(.*?)""
    // e\.parseJSON\(atob\(t\)\.slice\(1\)\)}\("(.*?)

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
            }
        }

        return builder.build();
    }

    protected String getAnimeURL(String url) throws IOException, InterruptedException, WebsiteException {
        if (isAnimeURL(url)) {
            return url;
        } else if (isEpisodeURL(url)) {
            Document document = getDocument(url);
            Element a = selectNonNull(document, "details > info > h1 > a");

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
            case "Terminé" -> Status.COMPLETED;
            case "En cours", "Pas encore commencé" -> Status.ONGOING;
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
                sources.add(new Source(url, subtitle, height, "m3u8"));
            }
        }
        br.close();

        return sources;
    }

    @Override
    public List<Anime> search() {
        return null;
    }
}
