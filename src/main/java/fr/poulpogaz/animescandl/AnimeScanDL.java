package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.model.Title;
import fr.poulpogaz.animescandl.utils.FakeUserAgent;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebDriver;
import fr.poulpogaz.animescandl.website.*;
import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnimeScanDL {

    public static final Website<?>[] WEBSITES = new Website[] {
            SushiScan.INSTANCE,
            Nekosama.INSTANCE,
            Mangadex.INSTANCE,
            Japscan.INSTANCE,
            Japanread.INSTANCE
    };

    private static final Logger LOGGER = LogManager.getLogger(AnimeScanDL.class);
    private static final Scanner scanner = new Scanner(System.in);

    private static final List<Settings> settings = new ArrayList<>();

    public static void run() {
        if (!loadConfig()) {
            LOGGER.fatal("Config is not valid");
        }

        // search pass
        for (int i = 0; i < settings.size(); i++) {
            Settings s = settings.get(i);

            if (!Utils.isValidURL(s.name())) {
                Title url = search(s);

                if (url == null) {
                    settings.remove(i);
                    i--;
                } else {
                    settings.set(i, url.createSettings(s));
                }
            }
        }

        // download pass
        for (Settings s : settings) {
            LOGGER.info("Processing {}", s.name());
            FakeUserAgent.newUserAgent();
            processSettings(s);
        }

        // dispose all resources
        for (Website<?> web : WEBSITES) {
            web.dispose();
        }

        WebDriver.dispose();
    }

    private static boolean loadConfig() {
        try {
            Path path = Path.of("animescandl.json");
            IJsonReader jr = new JsonReader(Files.newBufferedReader(path));
            jr.beginArray();

            while (!jr.isArrayEnd()) {
                jr.beginObject();

                Settings target = parseTarget(jr);

                if (target == null) {
                    LOGGER.warn("Can't parse target");
                    return false;
                }
                settings.add(target);

                jr.endObject();
            }

            jr.endArray();
            jr.close();

            return true;
        } catch (IOException | JsonException e) {
            LOGGER.warn("Failed to read config", e);
            return false;
        }
    }

    private static Settings parseTarget(IJsonReader jr) throws IOException, JsonException {
        Settings.Builder builder = new Settings.Builder();

        while (!jr.isObjectEnd()) {
            String key = jr.nextKey();

            switch (key) {
                case "name" -> builder.setName(jr.nextString());
                case "concatenateAll" -> builder.setConcatenateAll(jr.nextBoolean());
                case "range" -> builder.setRange(Utils.parseRange(jr.nextString()));
                case "language" -> builder.setLanguage(jr.nextString());
                case "out" -> builder.setOut(Path.of(jr.nextString()));
                default -> {
                    LOGGER.warn("Unknown scan attribute {}", key);
                    return null;
                }
            }
        }

        return builder.build();
    }

    private static void processSettings(Settings settings) {
        String url = settings.name();

        boolean websiteFound = false;
        for (Website<?> website : WEBSITES) {
            if (website.accept(url)) {
                try {
                    website.process(url, settings);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to download anime for {}", url, e);
                }

                websiteFound = true;
            }
        }

        if (!websiteFound) {
            LOGGER.warn("No website found for {}", url);
        }
    }

    private static Title search(Settings settings) {
        try {
            LOGGER.info("Searching for {}", settings.name());
            List<Title> results = new ArrayList<>();

            for (Website<?> website : WEBSITES) {
                results.addAll(website.search(settings.name(), settings));
            }

            if (results.size() == 0) {
                LOGGER.warn("No result. Skipping.");
                return null;
            } else if (results.size() == 1) {
                LOGGER.info("One result: {}", results.get(0));
                return results.get(0);
            } else {

                LOGGER.info("{} results:", results.size());
                for (int i = 0; i < results.size(); i++) {
                    Title r = results.get(i);

                    LOGGER.info("{}: [{}] {}", i + 1, r.website().name(), r);
                }

                int i = answerUser(results.size()) - 1;

                return results.get(i);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to search", e);
            return null;
        }
    }

    private static int answerUser(int max) {
        LOGGER.info("Which one would you like to download? Answer with a number");

        while (true) {
            if (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                    int i = scanner.nextInt();

                    if (i >= 1 && i <= max) {
                        return i;
                    }

                    LOGGER.info("I need a number between {} and {}! ୧((#Φ益Φ#))୨ ", 1, max);
                } else {
                    scanner.next();
                    LOGGER.info("I need a number! ୧((#Φ益Φ#))୨ ");
                }
            }
        }
    }
}
