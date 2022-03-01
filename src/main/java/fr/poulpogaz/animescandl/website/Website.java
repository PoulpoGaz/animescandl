package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.model.Title;

import java.util.List;

public interface Website<T extends Title> {

    String name();

    String version();

    String url();

    boolean accept(String url);

    void process(String url, Settings settings) throws Throwable;

    List<T> search(String search, Settings settings) throws Throwable;

    void dispose();
}