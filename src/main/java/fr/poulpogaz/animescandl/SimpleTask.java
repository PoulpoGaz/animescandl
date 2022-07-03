package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.math.Interval;
import fr.poulpogaz.animescandl.utils.math.Set;

import java.nio.file.Path;

public class SimpleTask extends Task {

    private final String name;

    public SimpleTask(String name, int number) {
        super(number);
        this.name = name;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String error() {
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean concatenateAll() {
        return false;
    }

    @Override
    public Set range() {
        return Interval.all();
    }

    @Override
    public String language() {
        return null;
    }

    @Override
    public Path out() {
        return null;
    }
}
