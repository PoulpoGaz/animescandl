package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.*;
import fr.poulpogaz.animescandl.scan.japscan.Japscan;
import fr.poulpogaz.animescandl.scan.mangadex.Mangadex;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnimeScanDownloader {

    private static final ASDLLogger LOGGER = Loggers.getLogger(AnimeScanDownloader.class);

    public static AnimeScanDownloader createDefault() throws IOException {
        AnimeScanDownloader a = new AnimeScanDownloader();
        a.addWebsite(new SushiScan());
        a.addWebsite(new Japanread());
        a.addWebsite(new MangaRead());
        a.addWebsite(new Mangadex());
        a.addWebsite(new Japscan());

        a.addWebsite(new Nekosama());

        return a;
    }

    private final List<Website> websites = new ArrayList<>();

    public AnimeScanDownloader() {

    }

    public void process(String url, Settings settings)
            throws WebsiteException, JsonException, IOException, InterruptedException {
        if (!HttpUtils.isValidURL(url)) {
            throw new WebsiteException("Not an url: " + url);
        }

        LOGGER.infoln("Processing: {}", url);

        Website w = getWebsiteFor(url);
        LOGGER.debugln("Website: " + w.name());

        if (w instanceof ScanWebsite<?,?> s) {
            downloadScan(s, url, settings);
        } else if (w instanceof AnimeWebsite<?,?>) {

        } else {
            throw new IllegalStateException("Unknown website type");
        }
    }

    protected Website getWebsiteFor(String url) throws WebsiteException {
        for (Website w : websites) {
            if (w.isSupported(url)) {
                return w;
            }
        }

        throw new WebsiteException("No website found for " + url);
    }

    protected <M extends Manga, C extends Chapter>
    void downloadScan(ScanWebsite<M, C> s, String url, Settings settings)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        if (s.supportLanguage()) {
            s.selectLanguage(settings.language());
        }

        M manga = s.getManga(url);
        List<C> chapters = s.getChapters(manga);

        ScanWriter sw = new ScanWriter(manga.getTitle(), settings.concatenateAll(), settings.out());

        for (C chap : chapters) {
            if (!settings.rangeContains((int) chap.getChapterNumber())) {
                continue;
            }

            String outName = chap.getName().orElse(manga.getTitle() + " - " + chap.getChapterNumber());
            Path out = settings.out().resolve(outName);

            if (Main.noOverwrites.isPresent() && Files.exists(out)) {
                continue;
            }

            sw.newScan(s, chap);
        }

        sw.endAll();
    }

    public void addWebsite(Website website) {
        websites.add(Objects.requireNonNull(website));
    }

    public List<Website> getWebsites() {
        return websites;
    }
}
