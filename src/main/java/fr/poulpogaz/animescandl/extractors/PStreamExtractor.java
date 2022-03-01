package fr.poulpogaz.animescandl.extractors;

import fr.poulpogaz.animescandl.Video;
import fr.poulpogaz.animescandl.utils.ExtractorException;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PStreamExtractor implements IExtractor {

    private static final Logger LOGGER = LogManager.getLogger(PStreamExtractor.class);

    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("e\\.parseJSON\\(atob\\(t\\)\\.slice\\(2\\)\\)}\\(\"(.*?)\"");
    // 27.12.2021 old version= "e\.parseJSON\(atob\(t\)\)}\("(.*?)""
    // e\.parseJSON\(atob\(t\)\.slice\(1\)\)}\("(.*?)

    @Override
    public String url() {
        return "www.pstream.net";
    }

    @Override
    public List<Video> extract(IRequestSender s, String url) throws ExtractorException, IOException, InterruptedException {
        Document document = s.getDocument(url);

        Element element = document.selectFirst("track[kind=subtitles]");
        String subtitles = null;
        if (element != null) {
            subtitles = element.attr("src");
        }

        LOGGER.debug("Subtitles: {}", subtitles);

        element = document.selectFirst("script[src^=https://www.pstream.net/u/player-script]");

        if (element == null) {
            throw new ExtractorException("Failed to extract video. Can't find videojs script");
        }

        String js = element.attr("src");
        InputStream is = s.getInputStream(js);
        if (is == null) {
            throw new ExtractorException("Failed to extract video");
        }

        String content = new String(is.readAllBytes());
        if (Utils.WRITE) {
            js = js.replace(":", "-").replace("/", "-");

            if (js.length() >= 256) {
                js = js.substring(0, 250);
            }

            Path out = Path.of(js + ".js");

            Files.writeString(out, content);
        }

        Matcher matcher = VIDEO_LINK_REGEX.matcher(content);
        if (matcher.find()) {
            String json = decode(matcher.group(1)).substring(2);

            LOGGER.debug("Decoded json:{}", json);

            String videoLink;
            try {
                videoLink = getURL(json);
            } catch (JsonException e) {
                throw new ExtractorException(e);
            }

            return List.of(new Video(videoLink, subtitles, 1080, Video.M3U8));
        }
        LOGGER.warn("Can't find video link in js");

        return null;
    }

    private String decode(String base64) {
        byte[] out = Base64.getDecoder().decode(base64);
        return new String(out);
    }

    private String getURL(String json) throws JsonException, IOException {
        JsonObject object = (JsonObject) JsonTreeReader.read(new StringReader(json));
        return object.getAsJsonString("real_url").getAsString();
    }
}