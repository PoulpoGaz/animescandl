package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.model.Episode;
import fr.poulpogaz.animescandl.model.Source;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.List;

public interface AnimeWebsite<A extends Anime, E extends Episode> extends Website {

    boolean isEpisodeURL(String url);

    boolean isAnimeURL(String url);

    default boolean isSupported(String url) {
        return isEpisodeURL(url) || isAnimeURL(url);
    }

    A getAnime(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    List<E> getEpisodes(A anime)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    List<Source> getSources(E episode) throws IOException, InterruptedException, JsonException, WebsiteException;

    List<A> search();
}
