package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.math.Set;

import java.nio.file.Path;

public abstract class Task {

    protected final int number;

    public Task(int number) {
        this.number = number;
    }

    public abstract boolean isValid();

    public abstract String error();

    public int number() {
        return number;
    }


    public abstract String name();

    public abstract boolean concatenateAll();

    public abstract Set range();

    public abstract String language();

    public abstract Path out();


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
