package fr.poulpogaz.animescandl.model;

public class DefaultEntry implements Entry {

    protected final String url;
    protected final int index;

    public DefaultEntry(String url, int index) {
        this.url = url;
        this.index = index;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public String toString() {
        return "DefaultEntry{" +
                "url='" + url + '\'' +
                ", index=" + index +
                '}';
    }
}
