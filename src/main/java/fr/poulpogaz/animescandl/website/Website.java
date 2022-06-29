package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.utils.IDocumentCache;
import fr.poulpogaz.animescandl.utils.IRequestSender;

public interface Website extends IRequestSender, IDocumentCache {

    String name();

    String url();

    String version();

    boolean isSupported(String url);

    default boolean needCEF() {
        return false;
    }
}
