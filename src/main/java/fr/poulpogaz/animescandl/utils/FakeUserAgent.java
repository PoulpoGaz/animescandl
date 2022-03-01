package fr.poulpogaz.animescandl.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * All chromes versions and user agent came from
 * {@link <a href="https://github.com/ytdl-org/youtube-dl/blob/cfee2dfe83c5593d46bd0c8e8ce6a3d8c6e42db7/youtube_dl/utils.py#L90">...</a>}
 */
public class FakeUserAgent {

    private static final Logger LOGGER = LogManager.getLogger(FakeUserAgent.class);

    private static final String USER_AGENT_FORMAT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Safari/537.36";
    private static final List<String> chromeVersions = new ArrayList<>();
    private static final String DEFAULT_CHROME_VERSION = "74.0.3729.129";
    private static String userAgent = null;

    public static String getUserAgent() {
        if (userAgent == null) {
            return newUserAgent();
        } else {
            return userAgent;
        }
    }

    public static String newUserAgent() {
        int n = (int) (Math.random() * chromeVersions.size());

        userAgent = USER_AGENT_FORMAT.formatted(chromeVersions.get(n));

        return userAgent;
    }

    static {
        InputStream is = FakeUserAgent.class.getResourceAsStream("/chromeversions");

        if (is == null) {
            chromeVersions.add(DEFAULT_CHROME_VERSION);

        } else {

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                String line;
                while ((line = br.readLine()) != null) {
                    chromeVersions.add(line);
                }

            } catch (IOException e) {
                LOGGER.warn("Failed to read chrome versions", e);
            }
        }
    }
}