package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Updater {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Updater.class);
    private static final String GET_RELEASES = "https://api.github.com/repos/PoulpoGaz/animescandl/releases";

    public static void update() {
        try {
            JsonArray releases = (JsonArray) HttpUtils.getJson(GET_RELEASES);
            JsonObject lastRelease = releases.getAsObject(0);

            if (isNewRelease(lastRelease)) {
                String downloadURL = getDownloadURL(lastRelease.getAsArray("assets"));

                LOGGER.debugln("Download url: {}", downloadURL);
                if (downloadURL != null) {
                    update(downloadURL);
                    return;
                }
            }

            LOGGER.infoln("No update available");
        } catch (JsonException | InterruptedException | IOException | URISyntaxException e) {
            LOGGER.warnln("Failed to update", e);
        }
    }

    private static boolean isNewRelease(JsonObject release) {
        String releaseName = release.getAsString("tag_name");

        return !releaseName.equals(Main.VERSION);
    }

    private static String getDownloadURL(JsonArray assets) {
        for (JsonElement e : assets) {
            JsonObject a = (JsonObject) e;

            String name = a.getAsString("name");

            if (name.contains("animescandl")) {
                return a.getAsString("browser_download_url");
            }
        }

        return null;
    }

    private static void update(String downloadURL) throws URISyntaxException, IOException, InterruptedException {
        URI path = Updater.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();

        InputStream is = HttpUtils.getInputStream(downloadURL);
        OutputStream os = Files.newOutputStream(Path.of(path));
        is.transferTo(os);
        os.close();
        is.close();
    }
}
