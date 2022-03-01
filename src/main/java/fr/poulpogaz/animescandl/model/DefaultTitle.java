package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.website.Settings;
import fr.poulpogaz.animescandl.website.Website;

public class DefaultTitle implements Title {

    protected final String url;
    protected final String name;
    protected final Website<?> website;

    public DefaultTitle(String url, String name, Website<?> website) {
        this.url = url;
        this.name = name;
        this.website = website;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Website<?> website() {
        return website;
    }

    @Override
    public Settings createSettings(Settings old) {
        return new Settings(url, old.range(), old.concatenateAll(), old.language(), old.out());
    }

    @Override
    public String toString() {
        return name;
    }
}
