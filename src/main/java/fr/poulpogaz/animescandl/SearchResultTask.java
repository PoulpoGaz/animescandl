package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.math.Set;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.InvalidValueException;

import java.nio.file.Path;

public class SearchResultTask extends Task {

    private final Task from;
    private final Object toDownload;

    public SearchResultTask(Task from, Object toDownload, Website website) {
        super(from.number());
        this.from = from;
        this.website = website;
        this.toDownload = toDownload;
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
