package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.anime.VideoDownloader;
import fr.poulpogaz.animescandl.model.*;
import fr.poulpogaz.animescandl.scan.*;
import fr.poulpogaz.animescandl.scan.japscan.Japscan;
import fr.poulpogaz.animescandl.scan.mangadex.Mangadex;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

import java.awt.*;
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
            throws WebsiteException, JsonException, IOException,
            InterruptedException, UnsupportedPlatformException, CefInitializationException {
        if (!HttpUtils.isValidURL(url)) {
            throw new WebsiteException("Not an url: " + url);
        }

        LOGGER.infoln("Processing: {}", url);

        Website w = getWebsiteFor(url);
        LOGGER.debugln("Website: " + w.name());

        if (w.needCEF()) {
            if (GraphicsEnvironment.isHeadless()) {
                throw new WebsiteException(w.name() + " doesn't support headless");
            }

            CEFHelper.initialize();
        }

        if (w instanceof ScanWebsite s) {
            downloadScan(s, url, settings);
        } else if (w instanceof AnimeWebsite a) {
            downloadAnime(a, url, settings);
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

    protected void downloadScan(ScanWebsite s, String url, Settings settings)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        if (s.supportLanguage()) {
            s.selectLanguage(settings.language());
        }

        Manga manga = s.getManga(url);
        List<Chapter> chapters = new ArrayList<>(s.getChapters(manga));
        chapters.sort(Comparator.comparingDouble(Chapter::getChapterNumber));

        AbstractScanWriter sw;

        if (s.isChapterURL(url) && settings.range() == null) {

            Chapter chapter = s.getChapter(chapters, url);
            sw = AbstractScanWriter.newWriter(getChapterName(chapter), settings.concatenateAll(), settings.out());

            downloadChapter(sw, s, chapter, settings);
        } else {
            sw = AbstractScanWriter.newWriter(manga.getTitle(), settings.concatenateAll(), settings.out());

            for (Chapter chap : chapters) {
                if (!settings.rangeContains(chap.getChapterNumber())) {
                    continue;
                }

                downloadChapter(sw, s, chap, settings);
            }
        }

        sw.endAll();
    }

    protected void downloadChapter(AbstractScanWriter sw, ScanWebsite s, Chapter chapter, Settings settings)
            throws JsonException, IOException, WebsiteException, InterruptedException {
        String chapName = getChapterName(chapter);
        String filename = chapName + ".pdf";
        Path out = settings.out().resolve(filename);

        if (Main.noOverwrites.isPresent() && Files.exists(out)) {
            LOGGER.infoln("{} already exists", filename);
            return;
        }

        LOGGER.infoln("Downloading {}", chapName);
        sw.newScan(s, chapter);
    }

    protected String getChapterName(Chapter chapter) {
        return chapter.getName().orElse(chapter.getManga().getTitle() + " - " + chapter.getChapterNumber());
    }

    protected void downloadAnime(AnimeWebsite a, String url, Settings settings)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        Anime anime = a.getAnime(url);
        List<Episode> episodes = a.getEpisodes(anime);

        if (a.isEpisodeURL(url) && settings.range() == null) {

            Episode episode = a.getEpisode(episodes, url);
            downloadEpisode(a, episode, settings);

        } else {
            for (Episode episode : episodes) {
                if (!settings.rangeContains(episode.getEpisode())) {
                    continue;
                }

                downloadEpisode(a, episode, settings);
            }
        }
    }

    protected void downloadEpisode(AnimeWebsite a, Episode episode, Settings settings)
            throws JsonException, IOException, WebsiteException, InterruptedException {
        List<Source> sources = a.getSources(episode);
        Source best = getBestSource(sources);

        if (best == null) {
            LOGGER.warnln("No anime found for {}", episode.getUrl());
            return;
        }

        String epName = episode.getName()
                .orElseGet(() -> episode.getAnime().getTitle() + " - " + episode.getEpisode());

        String filename = epName + ".mp4";
        Path out = settings.out().resolve(filename);

        if (Main.noOverwrites.isPresent() && Files.exists(out)) {
            LOGGER.infoln("{} already exists", filename);
            return;
        }

        LOGGER.infoln("Downloading {}", epName);
        VideoDownloader.download(episode, best, out);
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
