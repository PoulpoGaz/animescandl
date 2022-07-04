package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.model.Manga;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public abstract class Task {

    private static final ASDLLogger LOGGER = Loggers.getLogger(Task.class);

    protected final int number;
    protected Website website;

    public Task(int number) {
        this.number = number;
    }

    public SearchResultTask search()
            throws InvalidValueException, JsonException, IOException, WebsiteException, InterruptedException {
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

    private SearchResultTask doSearch(String search, Collection<Website> websites)
            throws JsonException, IOException, WebsiteException, InterruptedException {
        List<Pair<Website, List<?>>> results = new ArrayList<>();

        for (Website website : websites) {
            if (website instanceof SearchWebsite<?> searchWebsite) {
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
    private SearchResultTask doSearch(String search, Website website)
            throws InvalidValueException, JsonException, IOException, WebsiteException, InterruptedException {
        if (website instanceof SearchWebsite<?> searchWebsite) {
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

    public void download() {

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
