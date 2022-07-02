package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Episode;
import fr.poulpogaz.animescandl.model.Source;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public interface AnimeWebsite extends Website {

    boolean isEpisodeURL(String url);

    boolean isAnimeURL(String url);

    default boolean isSupported(String url) {
        return isEpisodeURL(url) || isAnimeURL(url);
    }

    Anime getAnime(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    List<Episode> getEpisodes(Anime anime)
            throws IOException, InterruptedException, WebsiteException, JsonException;

    default Episode getEpisode(List<Episode> allEpisodes, String url) {
        try {
            URI uri = new URI(url);

            for (Episode episode : allEpisodes) {
                URI uri2 = new URI(episode.getUrl());

                if (uri.equals(uri2)) {
                    return episode;
                }
            }

            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    List<Source> getSources(Episode episode) throws IOException, InterruptedException, JsonException, WebsiteException;
}
