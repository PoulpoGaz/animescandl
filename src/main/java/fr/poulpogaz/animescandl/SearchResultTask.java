package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.utils.math.Set;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.InvalidValueException;

import java.nio.file.Path;
import java.util.Objects;

public class SearchResultTask extends Task {

    private final Task from;
    private final Object toDownload;

    public SearchResultTask(Task from, Object toDownload, Website website) {
        super(Objects.requireNonNull(from).number());
        this.from = from;
        this.website = Objects.requireNonNull(website);
        this.toDownload = Objects.requireNonNull(toDownload);

        if (website instanceof AnimeWebsite && toDownload instanceof Manga) {
            throw new IllegalArgumentException("Can't download a manga from an anime website");
        }

        if (website instanceof ScanWebsite && toDownload instanceof Anime) {
            throw new IllegalArgumentException("Can't download an anime from a scan website");
        }

        if (!(toDownload instanceof Manga) && !(toDownload instanceof Anime)) {
            throw new IllegalArgumentException("toDownload must be an Anime or a Manga");
        }
    }

    @Override
    protected Manga getManga(ScanWebsite website) {
        return (Manga) toDownload;
    }

    @Override
    protected Anime getAnime(AnimeWebsite website) {
        return (Anime) toDownload;
    }

    @Override
    public boolean isValid() {
        return toDownload != null;
    }

    @Override
    public String error() {
        return null;
    }

    @Override
    public String name() {
        return from.name();
    }

    @Override
    public boolean concatenateAll() {
        return from.concatenateAll();
    }

    @Override
    public Set range() {
        return from.range();
    }

    @Override
    public String language() {
        return from.language();
    }

    @Override
    public Path out() {
        return from.out();
    }

    @Override
    protected void applyFilters(FilterList filter) throws InvalidValueException {
        throw new IllegalStateException();
    }
}
