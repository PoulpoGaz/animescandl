package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.anime.VideoDownloader;
import fr.poulpogaz.animescandl.model.*;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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

        if (w instanceof ScanWebsite<?, ?> s) {
            downloadScan(s, url, settings, createChapterFilter(settings, url, s));
        } else if (w instanceof AnimeWebsite<?, ?> a) {
            downloadAnime(a, url, settings, createEpisodeFilter(settings, url, a));
        } else {
            throw new IllegalStateException("Unknown website type");
        }
    }

    private Predicate<Chapter> createChapterFilter(Settings settings, String url, ScanWebsite<?, ?> w) {
        if (w.isChapterURL(url) && settings.range() == null) {
            return (c) -> c.getUrl().equals(url); // may not work for all websites. eg: mangadex
        } else {
            return (c) -> settings.rangeContains((int) c.getChapterNumber());
        }
    }

    private Predicate<Episode> createEpisodeFilter(Settings settings, String url, AnimeWebsite<?, ?> w) {
        if (w.isEpisodeURL(url) && settings.range() == null) {
            return (e) -> e.getUrl().equals(url);
        } else {
            return (e) -> settings.rangeContains(e.getEpisode());
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
    void downloadScan(ScanWebsite<M, C> s, String url, Settings settings, Predicate<Chapter> filter)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        if (s.supportLanguage()) {
            s.selectLanguage(settings.language());
        }

        M manga = s.getManga(url);
        List<C> chapters = s.getChapters(manga);
        chapters.sort(Comparator.comparingDouble(Chapter::getChapterNumber));

        ScanWriter sw = new ScanWriter(manga.getTitle(), settings.concatenateAll(), settings.out());

        for (C chap : chapters) {
            if (!filter.test(chap)) {
                continue;
            }

            String filename = chap.getName().orElse(manga.getTitle() + " - " + chap.getChapterNumber()) + ".pdf";
            Path out = settings.out().resolve(filename);

            if (Main.noOverwrites.isPresent() && Files.exists(out)) {
                LOGGER.infoln("{} already exists", filename);
                continue;
            }

            sw.newScan(s, chap);
        }

        sw.endAll();
    }

    protected <A extends Anime, E extends Episode>
    void downloadAnime(AnimeWebsite<A, E> a, String url, Settings settings, Predicate<Episode> filter)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        A anime = a.getAnime(url);
        List<E> episodes = a.getEpisodes(anime);

        for (E episode : episodes) {
            if (!filter.test(episode)) {
                continue;
            }

            List<Source> sources = a.getSources(episode);
            Source best = getBestSource(sources);

            if (best == null) {
                LOGGER.warnln("No anime found for {}", url);
                return;
            }

            String filename = episode.getName()
                    .orElseGet(() -> episode.getAnime().getTitle() + " - " + episode.getEpisode())
                    + ".mp4";

            Path out = settings.out().resolve(filename);
            if (Main.noOverwrites.isPresent() && Files.exists(out)) {
                LOGGER.infoln("{} already exists", filename);
                continue;
            }

            VideoDownloader.download(episode, best, out);
        }
    }

    protected Source getBestSource(List<Source> sources) {
        if (sources.size() == 0) {
            return null;
        }

        Source best = null;
        int quality = -2;
        for (Source curr : sources) {
            int currQuality = curr.getQuality().orElse(-1);

            if (currQuality > quality) {
                best = curr;
                quality = currQuality;
            }
        }

        return best;
    }

    public void addWebsite(Website website) {
        websites.add(Objects.requireNonNull(website));
    }

    public List<Website> getWebsites() {
        return websites;
    }
}
