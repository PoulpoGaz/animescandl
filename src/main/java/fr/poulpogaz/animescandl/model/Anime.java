package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.*;

public class Anime {

    private final String url;
    private final String title;
    private final String description;
    private final int nEpisode;
    private final List<String> studios;
    private final List<String> genres;
    private final Type type;
    private final Status status;
    private final String thumbnailURL;
    private final float score;

    public Anime(String url,
                 String title,
                 String description,
                 int nEpisode,
                 List<String> studios,
                 List<String> genres,
                 Type type,
                 Status status,
                 String thumbnailURL,
                 float score) {
        this.url = Objects.requireNonNull(url);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.nEpisode = nEpisode;
        this.studios = studios == null ? List.of() : Collections.unmodifiableList(studios);
        this.genres = genres == null ? List.of() : Collections.unmodifiableList(genres);
        this.type = type;
        this.status = status;
        this.thumbnailURL = thumbnailURL;
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<Integer> getNEpisode() {
        return nEpisode >= 0 ? Optional.of(nEpisode) : Optional.empty();
    }

    public List<String> getStudios() {
        return studios;
    }

    public List<String> getGenres() {
        return genres;
    }

    public Optional<Type> getType() {
        return Optional.ofNullable(type);
    }

    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<String> getThumbnailURL() {
        return Optional.ofNullable(thumbnailURL);
    }

    public Optional<Float> getScore() {
        return score < 0 ? Optional.empty() : Optional.of(score);
    }

    @Override
    public String toString() {
        return "Anime{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", nEpisode=" + nEpisode +
                ", studios=" + studios +
                ", genres=" + genres +
                ", type=" + type +
                ", status=" + status +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", score=" + score +
                '}';
    }

    public static class Builder {

        private String url;
        private String title;
        private String description;
        private int nEpisode;
        private List<String> studios = new ArrayList<>();
        private List<String> genres = new ArrayList<>();
        private Type type;
        private Status status;
        private String thumbnailURL;
        private float score;

        public Anime build() {
            if (url == null) {
                throw new BuilderException("URL is null");
            }

            if (title == null) {
                throw new BuilderException("title is null");
            }

            return new Anime(url, title, description, nEpisode, studios, genres, type, status, thumbnailURL, score);
        }

        public String getUrl() {
            return url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public int getNEpisode() {
            return nEpisode;
        }

        public Builder setNEpisode(int nEpisode) {
            this.nEpisode = nEpisode;
            return this;
        }

        public List<String> getStudios() {
            return studios;
        }

        public Builder setStudios(List<String> studios) {
            this.studios = studios;
            return this;
        }

        public Builder addStudio(String studio) {
            studios.add(studio);
            return this;
        }

        public List<String> getGenres() {
            return genres;
        }

        public Builder setGenres(List<String> genres) {
            this.genres = genres;
            return this;
        }

        public Builder addGenre(String genre) {
            genres.add(genre);
            return this;
        }

        public Type getType() {
            return type;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Status getStatus() {
            return status;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public String getThumbnailURL() {
            return thumbnailURL;
        }

        public Builder setThumbnailURL(String thumbnailURL) {
            this.thumbnailURL = thumbnailURL;
            return this;
        }

        public float getScore() {
            return score;
        }

        public Builder setScore(float score) {
            this.score = score;
            return this;
        }
    }
}
