package fr.poulpogaz.animescandl.website;

public interface Website {

    String name();

    String url();

    String version();

    default boolean supportHeadless() {
        return true;
    }
}
