package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.Objects;
import java.util.Optional;

public class Episode {

    private final Anime anime;
    private final String url;

    private final int episode;
    private final String name;

    public Episode(Anime anime, String url, int episode, String name) {
        this.anime = Objects.requireNonNull(anime);
        this.url = Objects.requireNonNull(url);
        this.episode = episode;
        this.name = name;
    }

    public Anime getAnime() {
        return anime;
    }

    public String getUrl() {
        return url;
    }

    public int getEpisode() {
        return episode;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public String toString() {
        return "Episode{" +
                "anime=" + anime +
                ", url='" + url + '\'' +
                ", episode=" + episode +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Builder {

        private Anime anime;
        private String url;

        private int episode;
        private String name;

        public Episode build() {
            if (anime == null) {
                throw new BuilderException("Anime is null");
            }

            if (url == null) {
                throw new BuilderException("URL is null");
            }

            return new Episode(anime, url, episode, name);
        }

        public Anime getAnime() {
            return anime;
        }

        public Builder setAnime(Anime anime) {
            this.anime = anime;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public int getEpisode() {
            return episode;
        }

        public Builder setEpisode(int episode) {
            this.episode = episode;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }
    }
}
