package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.args.*;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.utils.Updater;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.Log4j2Setup;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
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

public class Main {

    public static final String VERSION = "1.4.2";
    public static final Path CONFIG_PATH = Path.of("animescandl.json");

    private static final String USAGE = """
            First create a file named "animescandl.json" next to animescandl executable.
            It is a json file which looks like this:

            [
                {
                    "name": "https://neko-sama.fr/anime/info/4973-steins-gate-vostfr",
                    "range": "1,5-7"
                },
                {
                    "name": "Re:zero",
                    "concatenateAll": true,
                    "out": "PATH"
                }
            ]

            It is an array of objects containing at least a "name". All others "arguments" are optional.
            It accepts the following arguments:
              - name: can be a direct url to a website or a just a string.\s
                In the last case, the program will search on all websites a scan/anime matching\s
                the search string.
              - range: A range looks like "1,5-7". It specifies which scans/animes
                will be downloaded.
              - concatenateAll: For scans, it will concatenate all scans and produce a big pdf
              - out: A folder where you want to download.
            """;

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static final Option supportedWebsite;
    public static final Option help;
    public static final Option version;
    public static final Option update;

    public static final Option ffmpeg;
    public static final Option operaDriver;
    public static final Option noOverwrites;

    public static final Option verbose;
    public static final Option writeLog;
    public static final Option simulate;
    public static final Option writeWebPages;

    static {
        supportedWebsite = new OptionBuilder()
                .name("supported-website")
                .shortName("sw")
                .desc("Print the list of supported website and exit")
                .build();
        help = new OptionBuilder()
                .name("help")
                .shortName("h")
                .desc("Print this message and exit")
                .build();
        version = new OptionBuilder()
                .name("version")
                .desc("Print version and exit")
                .build();
        update = new OptionBuilder()
                .name("update")
                .shortName("U")
                .desc("Update this program to latest version")
                .build();

        ffmpeg = new OptionBuilder()
                .name("ffmpeg")
                .shortName("ff")
                .argName("path")
                .desc("Path to ffmpeg")
                .build();
        operaDriver = new OptionBuilder()
                .name("opera")
                .shortName("op")
                .argName("path")
                .desc("Path to opera driver")
                .build();
        noOverwrites = new OptionBuilder()
                .name("no-overwrites")
                .shortName("w")
                .desc("Do not overwrite files")
                .build();

        // DEBUG
        verbose = new OptionBuilder()
                .name("verbose")
                .shortName("v")
                .desc("be extra verbose")
                .build();
        writeLog = new OptionBuilder()
                .name("write-log")
                .shortName("wl")
                .desc("Write log to a file")
                .build();
        simulate = new OptionBuilder()
                .name("simulate")
                .shortName("s")
                .desc("Do not download anime or scan")
                .build();
        writeWebPages = new OptionBuilder()
                .name("write-webpages")
                .shortName("ww")
                .desc("Write all webpages to disk")
                .build();
    }



    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(version)
                .addOption(help)
                .addOption(supportedWebsite)
                .addOption(update);

        options.addOption("System", operaDriver)
                .addOption("System", ffmpeg)
                .addOption("System", noOverwrites);

        options.addOption("Debug", verbose)
                .addOption("Debug", writeLog)
                .addOption("Debug", simulate)
                .addOption("Debug", writeWebPages);

        try {
            options.parse(args);
        } catch (ParseException e) {
            e.printStackTrace();

            HelpFormatter.printHelp(USAGE, options);
            return;
        }

        Log4j2Setup.setup(verbose.isPresent(), writeLog.isPresent());
        LOGGER.debug("================================ AnimeScanDL ================================");


        AnimeScanDL a;
        try {
            a = AnimeScanDL.createDefault();
        } catch (IOException e) {
            System.err.println("FATAL");
            throw new RuntimeException(e);
        }



        if (help.isPresent()) {
            HelpFormatter.printHelp(USAGE, options);
            return;
        }
        if (version.isPresent()) {
            System.out.println(VERSION);
            return;
        }
        if (supportedWebsite.isPresent()) {
            for (Website website : a.getWebsites()) {
                System.out.printf("%s [%s]\n", website.name(), website.version());
            }

            return;
        }


        if (update.isPresent()) {
            Updater.update();
        } else {

            try {
                for (Pair<String, Settings> p : loadConfig()) {
                    a.process(p.left(), p.right());
                }

            } catch (WebsiteException | JsonException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<Pair<String, Settings>> loadConfig() throws JsonException, IOException {
        List<Pair<String, Settings>> settings = new ArrayList<>();

        IJsonReader jr = new JsonReader(Files.newBufferedReader(CONFIG_PATH));
        jr.beginArray();

        while (!jr.isArrayEnd()) {
            jr.beginObject();

            Pair<String, Settings> target = parseTarget(jr);

            settings.add(target);

            jr.endObject();
        }

        jr.endArray();
        jr.close();

        return settings;
    }

    private static Pair<String, Settings> parseTarget(IJsonReader jr) throws IOException, JsonException {
        String url = null;
        Settings.Builder builder = new Settings.Builder();

        while (!jr.isObjectEnd()) {
            String key = jr.nextKey();

            switch (key) {
                case "name" -> url = jr.nextString();
                case "concatenateAll" -> builder.setConcatenateAll(jr.nextBoolean());
                case "range" -> builder.setRange(Utils.parseRange(jr.nextString()));
                case "language" -> builder.setLanguage(jr.nextString());
                case "out" -> builder.setOut(Path.of(jr.nextString()));
                default -> throw new IOException("Unknown attribute: " + key);
            }
        }

        if (url == null) {
            throw new IOException("URL is null");
        }

        return new Pair<>(url, builder.build());
    }
}