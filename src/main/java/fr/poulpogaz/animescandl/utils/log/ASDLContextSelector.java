package fr.poulpogaz.animescandl.utils.log;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;

import java.net.URI;

public class ASDLContextSelector extends ClassLoaderContextSelector {

    @Override
    protected LoggerContext createContext(final String name, final URI configLocation) {
        return new ASDLLoggerContext(name, null, configLocation);
    }

    @Override
    protected String toContextMapKey(final ClassLoader loader) {
        return "ASDLContextSelector@" + Integer.toHexString(System.identityHashCode(loader));
    }

    @Override
    protected String defaultContextName() {
        return "ASDLContextSelector@" + Thread.currentThread().getName();
    }
}
