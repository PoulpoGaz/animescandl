package fr.poulpogaz.animescandl.utils.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class Log4j2Setup {

    private static final String CONSOLE = "console";
    private static final String FILE = "file";
    private static final String LOG_FILE = "log.log";

    private static final String VERBOSE_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %replace{%msg}{[\r\n]+}{}%n";


    public static void setup(boolean verbose, boolean writeLogs) {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.INFO);

        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.TRACE);

        if (verbose) {
            createVerboseConsole(builder);
            rootLogger.add(
                    builder.newAppenderRef(CONSOLE)
                            .addAttribute("level", Level.TRACE));
        } else {
            createConsole(builder);
            rootLogger.add(
                    builder.newAppenderRef(CONSOLE)
                            .addAttribute("level", Level.INFO));
        }

        if (writeLogs) {
            createFile(builder);
            rootLogger.add(builder.newAppenderRef(FILE));
        }

        builder.add(rootLogger);

        ((LoggerContext) LogManager.getContext(false))
                .reconfigure(builder.build());
    }

    private static void createVerboseConsole(ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", VERBOSE_PATTERN);

        builder.add(
                builder.newAppender(CONSOLE, "Console")
                        .add(layout)
        );
    }

    private static void createConsole(ConfigurationBuilder<BuiltConfiguration> builder) {
        ComponentBuilder<?> v = builder.newComponent("LevelPatternSelector");
        v.addComponent(patternMatch(builder, "INFO", "%m%throwable{short}"));
        v.addComponent(patternMatch(builder, "WARN", "[%-5level] %m%throwable{short}"));
        v.addComponent(patternMatch(builder, "ERROR", "[%-5level] %m%throwable{short}"));
        v.addComponent(patternMatch(builder, "FATAL", "[%-5level] %m%throwable{short}"));

        LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addComponent(v);

        builder.add(
                builder.newAppender(CONSOLE, "Console")
                        .add(layout)
        );
    }

    private static ComponentBuilder<?> patternMatch(ConfigurationBuilder<BuiltConfiguration> builder, String key, String pattern) {
        return builder.newComponent("PatternMatch")
                .addAttribute("key", key)
                .addAttribute("pattern", pattern);
    }

    private static void createFile(ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", VERBOSE_PATTERN);

        builder.add(
                builder.newAppender(FILE, "File")
                        .addAttribute("fileName", LOG_FILE)
                        .addAttribute("ignoreExceptions", "false")
                        .addAttribute("append", "true")
                        .add(layout)
        );
    }
}
