package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.website.Settings;
import fr.poulpogaz.animescandl.website.Website;

public interface Title {

    String url();

    String name();

    Website<?> website();

    Settings createSettings(Settings oldSettings);
}
