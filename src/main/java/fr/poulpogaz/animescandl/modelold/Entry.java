package fr.poulpogaz.animescandl.modelold;

public interface Entry {

    /**
     * Not necessarily an url.
     * Can also be an id {@link fr.poulpogaz.animescandl.websiteold.Mangadex}
     */
    String url();

    int index();
}
