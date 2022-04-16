package fr.poulpogaz.animescandl.modelold;

import fr.poulpogaz.animescandl.websiteold.Settings;
import fr.poulpogaz.animescandl.websiteold.Website;

public interface Title {

    String url();

    String name();

    Website<?> website();

    Settings createSettings(Settings oldSettings);
}
