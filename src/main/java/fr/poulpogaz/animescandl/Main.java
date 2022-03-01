package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.args.*;
import fr.poulpogaz.animescandl.utils.Log4j2Setup;
import fr.poulpogaz.animescandl.utils.Updater;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebDriver;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.Website;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    public static final String VERSION = "1.4.1";

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

    public static void main(String[] args) {
        Option supportedWebsite = new OptionBuilder()
                .name("supported-website")
                .shortName("sw")
                .desc("Print the list of supported website and exit")
                .build();
        Option help = new OptionBuilder()
                .name("help")
                .shortName("h")
                .desc("Print this message and exit")
                .build();
        Option version = new OptionBuilder()
                .name("version")
                .desc("Print version and exit")
                .build();
        Option update = new OptionBuilder()
                .name("update")
                .shortName("U")
                .desc("Update this program to latest version")
                .build();
        Option verbose = new OptionBuilder()
                .name("verbose")
                .shortName("v")
                .desc("be extra verbose")
                .build();
        Option writeLog = new OptionBuilder()
                .name("write-log")
                .shortName("wl")
                .desc("Write log to a file")
                .build();
        Option simulate = new OptionBuilder()
                .name("simulate")
                .shortName("s")
                .desc("Do not download anime or scan")
                .build();
        Option writeWebPages = new OptionBuilder()
                .name("write-webpages")
                .shortName("ww")
                .desc("Write all webpages to disk")
                .build();
        Option ffmpeg = new OptionBuilder()
                .name("ffmpeg")
                .shortName("ff")
                .argName("path")
                .desc("Path to ffmpeg")
                .build();
        Option operaDriver = new OptionBuilder()
                .name("opera")
                .shortName("op")
                .argName("path")
                .desc("Path to opera driver")
                .build();

        Options options = new Options();
        options.addOption(version)
                .addOption(help)
                .addOption(supportedWebsite)
                .addOption(update);
        options.addOption("System", operaDriver)
                .addOption("System", ffmpeg);
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

        if (help.isPresent()) {
            HelpFormatter.printHelp(USAGE, options);
            return;
        }
        if (version.isPresent()) {
            System.out.println(VERSION);
            return;
        }
        if (supportedWebsite.isPresent()) {
            for (Website<?> website : AnimeScanDL.WEBSITES) {
                System.out.printf("%s [%s]\n", website.name(), website.version());
            }
            return;
        }

        Utils.VERBOSE = verbose.isPresent();
        AbstractWebsite.NO_DOWNLOAD = simulate.isPresent();
        Utils.WRITE = writeWebPages.isPresent();
        Utils.FFMPEG_PATH = ffmpeg.getArgument(0).orElse("ffmpeg");
        WebDriver.OPERA_DRIVER_PATH = operaDriver.getArgument(0).orElse("drivers/operadriver");

        Log4j2Setup.setup(Utils.VERBOSE, writeLog.isPresent());
        LOGGER.debug("================================ AnimeScanDL ================================");

        if (update.isPresent()) {
            Updater.update();
        } else {
            AnimeScanDL.run();
        }
    }
}