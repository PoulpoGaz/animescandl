package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.scan.MangaRead;
import fr.poulpogaz.animescandl.scan.SushiScan;
import fr.poulpogaz.animescandl.scan.japscan.Japscan;
import fr.poulpogaz.animescandl.scan.mangadex.Mangadex;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.Website;

import java.util.*;

public class Websites {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Websites.class);

    public static final Map<String, Website> WEBSITES;

    static {
        Map<String, Website> websites = new HashMap<>();
        addWebsite(websites, new SushiScan());

        //try {
        //    addWebsite(websites, new Japanread());
        //} catch (IOException e) {
        //    LOGGER.errorln("Failed to initialize Japanread.");
        //    LOGGER.errorln("You won't be able to download scans from Japanread.", e);
        //}

        addWebsite(websites, new MangaRead());
        addWebsite(websites, new Mangadex());
        addWebsite(websites, new Japscan());

        addWebsite(websites, new Nekosama());

        WEBSITES = Collections.unmodifiableMap(websites);
    }

    private static void addWebsite(Map<String, Website> websites, Website website) {
        websites.put(website.name().toLowerCase(), website);
    }

    public static Website getWebsite(String name) {
        return WEBSITES.get(name.toLowerCase());
    }

    public static Collection<Website> getWebsites() {
        return WEBSITES.values();
    }

    public static Website[] getWebsitesArray() {
        return getWebsites().toArray(new Website[0]);
    }
}
