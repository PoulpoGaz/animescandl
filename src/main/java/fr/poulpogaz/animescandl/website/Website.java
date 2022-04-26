package fr.poulpogaz.animescandl.website;

public interface Website {

    String name();

    String url();

    String version();

    boolean isSupported(String url);

    default boolean supportHeadless() {
        return true;
    }
}
