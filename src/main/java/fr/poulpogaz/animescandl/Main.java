package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.args.*;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.WebDriver;
import fr.poulpogaz.animescandl.website.AbstractWebsite;
import fr.poulpogaz.animescandl.website.Website;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Main {

    private static final String VERSION = "1.4";

    // TODO: usage
    private static final String USAGE = "";

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
                .addOption(supportedWebsite);
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

        if (verbose.isPresent()) {
            System.setProperty("stdout", "verbose");
            System.setProperty("stdout-level", "trace");
            Utils.VERBOSE = true;
        }
        if (writeLog.isPresent()) {
            System.setProperty("write-log", "true");
        }

        AbstractWebsite.NO_DOWNLOAD = simulate.isPresent();
        Utils.WRITE = writeWebPages.isPresent();
        Utils.FFMPEG_PATH = ffmpeg.getArgument(0).orElse("ffmpeg");
        WebDriver.OPERA_DRIVER_PATH = operaDriver.getArgument(0).orElse("drivers/operadriver");

        ((LoggerContext) LogManager.getContext(false)).reconfigure();

        LOGGER.debug("================================ AnimeScanDL ================================");
        AnimeScanDL.run();
    }
}