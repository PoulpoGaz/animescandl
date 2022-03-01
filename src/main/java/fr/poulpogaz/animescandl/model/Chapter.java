package fr.poulpogaz.animescandl.model;

public class Chapter extends DefaultEntry {

    protected final String chapter;

    public Chapter(String url, int index, String chapter) {
        super(url, index);
        this.chapter = chapter;
    }

    public String chapter() {
        return chapter;
    }
}
