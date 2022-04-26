package fr.poulpogaz.animescandl.utils.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loggers {

    private static ASDLLogger cast(Logger logger) {
        if (logger instanceof ASDLLogger l) {
            return l;
        }

        throw new IllegalStateException("Log4j2 configuration problems. " +
                "Expected " + ASDLLogger.class + " but was " + logger.getClass());
    }

    public static ASDLLogger getLogger(Class<?> class_) {
        return cast(LogManager.getLogger(class_));
    }

    public static ASDLLogger getLogger(String name) {
        return cast(LogManager.getLogger(name));
    }
}
