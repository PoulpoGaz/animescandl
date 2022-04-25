package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.utils.IRequestSender;

public interface Website extends IRequestSender {

    String name();

    String url();

    String version();

    default boolean supportHeadless() {
        return true;
    }
}
