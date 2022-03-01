package fr.poulpogaz.animescandl.model;

public interface Entry {

    /**
     * Not necessarily an url.
     * Can also be an id {@link fr.poulpogaz.animescandl.website.Mangadex}
     */
    String url();

    int index();
}
