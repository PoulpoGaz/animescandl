package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.VideoDownloader;
import fr.poulpogaz.animescandl.model.*;
import fr.poulpogaz.animescandl.scan.AbstractScanWriter;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.utils.CEFHelper;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.utils.math.Set;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.InvalidValueException;
import fr.poulpogaz.json.JsonException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public abstract class Task {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Task.class);

    protected final int number;
    protected Website website;

    public Task(int number) {
        this.number = number;
    }

    public SearchResultTask search() throws InvalidValueException,
            JsonException, IOException, WebsiteException, InterruptedException,
            UnsupportedPlatformException, CefInitializationException {
        String name = name();
        int colon = name.indexOf(':');

        if (colon < 0) {
            return doSearch(name(), Websites.getWebsites());
        } else {
            String website = name.substring(0, colon);

            Website w = Websites.getWebsite(website);

            if (w == null) {
                return doSearch(name(), Websites.getWebsites());
            } else {
                return doSearch(name().substring(colon + 1), w);
            }
        }
    }

    private SearchResultTask doSearch(String search, Collection<Website> websites) throws JsonException,
            IOException, WebsiteException, InterruptedException,
            UnsupportedPlatformException, CefInitializationException {
        List<Pair<Website, List<?>>> results = new ArrayList<>();

        for (Website website : websites) {
            if (website instanceof SearchWebsite<?> searchWebsite) {
                initializeCEFIfNeeded(website);
                List<?> r = searchWebsite.search(search, searchWebsite.getSearchFilter());

                if (!r.isEmpty()) {
                    results.add(new Pair<>(website, r));
                }
            }
        }

        return askUser(results);
    }

    /**
     * @param website a website choose by the user
     */
    private SearchResultTask doSearch(String search, Website website) throws InvalidValueException,
            JsonException, IOException, WebsiteException, InterruptedException,
            UnsupportedPlatformException, CefInitializationException {
        if (website instanceof SearchWebsite<?> searchWebsite) {
            initializeCEFIfNeeded(website);

            FilterList filterList = searchWebsite.getSearchFilter();
            applyFilters(filterList);

            List<?> result = searchWebsite.search(search, filterList);

            if (result.isEmpty()) {
                LOGGER.warnln("No result");
                return null;
            }

            return askUser(List.of(new Pair<>(website, result)));
        } else {
            LOGGER.errorln("{}: website does not support search", website);
            return null;
        }
    }

    private SearchResultTask askUser(List<Pair<Website, List<?>>> searchResult) {
        if (searchResult.size() == 0) {
            LOGGER.infoln("No result");
            return null;
        } else if (searchResult.size() == 1) {
            Pair<Website, List<?>> result = searchResult.get(0);

            if (result.right().size() == 1) {
                return new SearchResultTask(this, result.right().get(0), result.left());
            }
        }

        Scanner sc = new Scanner(System.in);

        int userChoice = -1;
        long nResults = searchResult.stream().mapToLong((p) -> p.right().size()).sum();

        print(searchResult);
        while (userChoice < 1 || userChoice > nResults) {
            if (sc.hasNextLine()) {
                String str = sc.nextLine();

                if (str.isEmpty() || str.isBlank()) {
                    continue;
                }

                Optional<Integer> integer = Utils.parseInt(str);

                if (integer.isPresent()) {
                    userChoice = integer.get();

                    if (userChoice < 1 || userChoice > nResults) {
                        LOGGER.warnln("Invalid index");
                    }
                } else {
                    LOGGER.warnln("Not an integer");
                }
            }
        }

        return getTask(userChoice - 1, searchResult);
    }

    private void print(List<Pair<Website, List<?>>> searchResult) {
        int index = 1;
        for (Pair<Website, List<?>> pair : searchResult) {
            String website = pair.left().name();

            for (Object o : pair.right()) {
                if (o instanceof Anime anime) {
                    Optional<Float> score = anime.getScore();

                    if (score.isPresent()) {
                        LOGGER.infoln("[{}] - {} - {} ({}☆)", index, website, anime.getTitle(), score.get());
                    } else {
                        LOGGER.infoln("[{}] - {} - {}", index, website, anime.getTitle());
                    }
                } else if (o instanceof Manga manga) {
                    Optional<Float> score = manga.getScore();

                    if (score.isPresent()) {
                        LOGGER.infoln("[{}] - {} - {} ({}☆)", index, website, manga.getTitle(), score.get());
                    } else {
                        LOGGER.infoln("[{}] - {} - {}", index, website, manga.getTitle());
                    }
                }

                index++;
            }
        }
    }

    private SearchResultTask getTask(int userChoice, List<Pair<Website, List<?>>> searchResult) {
        int size = 0;
        for (Pair<Website, List<?>> p : searchResult) {
            int lastSize = size;
            size += p.right().size();

            if (size > userChoice) {
                Object toDownload = p.right().get(userChoice - lastSize);

                return new SearchResultTask(this, toDownload, p.left());
            }
        }

        throw new IllegalStateException();
    }



    public void download() throws JsonException, IOException, WebsiteException, InterruptedException,
            UnsupportedPlatformException, CefInitializationException {
        Website website = getWebsiteFor();
        LOGGER.debugln("Downloading from {}", website);

        if (website.needCEF()) {
            if (GraphicsEnvironment.isHeadless()) {
                throw new WebsiteException(website.name() + " doesn't support headless");
            }

            CEFHelper.initialize();
        }

        if (website instanceof ScanWebsite s) {
            downloadScan(s);
        } else if (website instanceof AnimeWebsite a) {
            downloadAnime(a);
        } else {
            throw new IllegalStateException("Unknown website type");
        }
    }

    protected void downloadScan(ScanWebsite s)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        if (s.supportLanguage()) {
            s.selectLanguage(language());
        }

        Manga manga = getManga(s);
        List<Chapter> chapters = new ArrayList<>(s.getChapters(manga));
        chapters.sort(Comparator.comparingDouble(Chapter::getChapterNumber));

        AbstractScanWriter sw;

        if (s.isChapterURL(name()) && range() == null) {

            Chapter chapter = s.getChapter(chapters, name());
            sw = AbstractScanWriter.newWriter(getChapterName(chapter), concatenateAll(), out());

            downloadChapter(sw, s, chapter);
        } else {
            sw = AbstractScanWriter.newWriter(manga.getTitle(), concatenateAll(), out());

            for (Chapter chap : chapters) {
                if (notInRange(chap.getChapterNumber())) {
                    continue;
                }

                downloadChapter(sw, s, chap);
            }
        }

        sw.endAll();
    }

    protected Manga getManga(ScanWebsite website) throws JsonException, IOException, WebsiteException, InterruptedException {
        return website.getManga(name());
    }

    protected void downloadChapter(AbstractScanWriter sw, ScanWebsite s, Chapter chapter)
            throws JsonException, IOException, WebsiteException, InterruptedException {
        String chapName = getChapterName(chapter);
        String filename = chapName + ".pdf";
        Path out = out().resolve(filename);

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

    protected void downloadAnime(AnimeWebsite a)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        Anime anime = getAnime(a);
        List<Episode> episodes = a.getEpisodes(anime);

        if (a.isEpisodeURL(name()) && range() == null) {

            Episode episode = a.getEpisode(episodes, name());
            downloadEpisode(a, episode);

        } else {
            for (Episode episode : episodes) {
                if (notInRange(episode.getEpisode())) {
                    continue;
                }

                downloadEpisode(a, episode);
            }
        }
    }

    protected Anime getAnime(AnimeWebsite website) throws JsonException, IOException, WebsiteException, InterruptedException {
        return website.getAnime(name());
    }

    protected void downloadEpisode(AnimeWebsite a, Episode episode)
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
        Path out = out().resolve(filename);

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



    private void initializeCEFIfNeeded(Website website)
            throws WebsiteException, UnsupportedPlatformException, CefInitializationException,
            IOException, InterruptedException {
        if (website.needCEF()) {
            if (GraphicsEnvironment.isHeadless()) {
                throw new WebsiteException(website.name() + " doesn't support headless");
            }

            CEFHelper.initialize();
        }
    }

    public abstract boolean isValid();

    public abstract String error();

    public int number() {
        return number;
    }


    public boolean isSearchTask() {
        return getWebsiteFor() == null;
    }

    public Website getWebsiteFor() {
        if (website != null) {
            return website;
        }

        for (Website w : Websites.getWebsites()) {
            if (w.isSupported(name())) {
                website = w;
                break;
            }
        }

        return website;
    }

    public abstract String name();

    public abstract boolean concatenateAll();

    public abstract Set range();

    public abstract String language();

    public abstract Path out();


    protected abstract void applyFilters(FilterList filter) throws InvalidValueException;


    public boolean inRange(float n) {
        if (range() == null) {
            return true;
        }

        return range().contains(n);
    }

    public boolean notInRange(float n) {
        return !inRange(n);
    }
}
