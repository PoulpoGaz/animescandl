package fr.poulpogaz.animescandl.website.filter;

import fr.poulpogaz.json.tree.JsonElement;

public abstract class Filter<T> {

    protected final String name;
    protected T value;

    public Filter(String name) {
        this.name = name;
    }

    public Filter(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public abstract void setValue(JsonElement value) throws InvalidValueException;

    public String getName() {
        return name;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
