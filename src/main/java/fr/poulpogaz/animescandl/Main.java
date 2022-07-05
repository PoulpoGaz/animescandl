package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.args.HelpFormatter;
import fr.poulpogaz.animescandl.args.Option;
import fr.poulpogaz.animescandl.args.OptionBuilder;
import fr.poulpogaz.animescandl.args.Options;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.ParseException;
import fr.poulpogaz.animescandl.utils.Updater;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Log4j2Setup;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.logging.log4j.Level;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.setEnabled;

public class Main {

    public static final String VERSION = "1.5-dev";
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

    private static final ASDLLogger LOGGER = Loggers.getLogger(Main.class);

    public static final Option supportedWebsite;
    public static final Option help;
    public static final Option version;
    public static final Option update;

    public static final Option ffmpeg;
    public static final Option noOverwrites;

    public static final Option verbose;
    public static final Option writeLog;
    public static final Option simulate;
    public static final Option writeWebPages;

    public static final Option enableAnsi;
    public static final Option disableAnsi;

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

        // OTHER
        enableAnsi = new OptionBuilder()
                .name("enable-ansi")
                .desc("Enable ansi code")
                .build();
        disableAnsi = new OptionBuilder()
                .name("disable-ansi")
                .desc("Disable ansi code")
                .build();
    }



    public static void main(String[] args) {
        try {
            AnsiConsole.systemInstall();
            mainImpl(args);
        } finally {
            CEFHelper.shutdowgin();
            AnsiConsole.systemUninstall();
        }
    }

    private static void mainImpl(String[] args) {
        Options options = createOptions();

        // parsing
        String result = options.parse(args);

        if (enableAnsi.isPresent() && !disableAnsi.isPresent()) {
            setEnabled(true);
        } else if (disableAnsi.isPresent() && !enableAnsi.isPresent()) {
            setEnabled(false);
        }

        // START!
        Log4j2Setup.setup(verbose.isPresent(), writeLog.isPresent());
        LOGGER.debugln("================================ AnimeScanDL ================================");

        // options that terminate the program after
        if (help.isPresent()) {
            LOGGER.infoln(HelpFormatter.helpString(USAGE, options));
            return;
        }

        if (version.isPresent()) {
            System.out.println(VERSION);
            return;
        }

        if (supportedWebsite.isPresent()) {
            for (Website website : Websites.getWebsites()) {
                LOGGER.infoln("{} [{}]", website.name(), website.version());
            }

            return;
        }

        // check for parsing errors
        if (result != null) {
            System.err.println(result);
            return;
        }

        if (update.isPresent()) {
            Updater.update();
        } else {

            if (Files.notExists(CONFIG_PATH)) {
                LOGGER.fatalln("Config file not found");
                return;
            }

            run();
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(version)
                .addOption(help)
                .addOption(supportedWebsite)
                .addOption(update);

        options.addOption("System", ffmpeg)
                .addOption("System", noOverwrites);

        options.addOption("Debug", verbose)
                .addOption("Debug", writeLog)
                .addOption("Debug", simulate)
                .addOption("Debug", writeWebPages);

        options.addOption("Other", disableAnsi)
                .addOption("Other", enableAnsi);

        return options;
    }

    private static void run() {
        Configuration configuration = new Configuration();

        try {
            configuration.load(CONFIG_PATH);
        } catch (IOException | JsonException e) {
            LOGGER.fatalln(e);
            return;
        } catch (ParseException e) {
            LOGGER.fatalln(e.getMessage());
            return;
        }

        try {
            configuration.search();
            configuration.download();
        } catch (UnsupportedPlatformException | CefInitializationException e) {
            LOGGER.fatalln("Failed to initialize CEF", e);
        } catch (InterruptedException e) {
            LOGGER.throwing(Level.INFO, e);
        }
    }
}