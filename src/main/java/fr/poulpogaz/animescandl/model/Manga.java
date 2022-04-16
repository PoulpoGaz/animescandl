package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Manga {

    private final String url;
    private final String title;
    private final String artist;
    private final String author;
    private final String description;
    private final List<String> genre;
    private final Status status;
    private final String thumbnailURL;
    private final float score;

    public Manga(String url, String title) {
        this(url, title, null, null, null, null, null, null, -1);
    }

    public Manga(String url,
                 String title,
                 String artist,
                 String author,
                 String description,
                 List<String> genre,
                 Status status,
                 String thumbnailURL,
                 float score) {
        this.url = url;
        this.title = title;
        this.artist = artist;
        this.author = author;
        this.description = description;
        this.genre = genre == null ? List.of() : Collections.unmodifiableList(genre);
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

    public Optional<String> getArtist() {
        return Optional.of(artist);
    }

    public Optional<String> getAuthor() {
        return Optional.of(author);
    }

    public Optional<String> getDescription() {
        return Optional.of(description);
    }

    public List<String> getGenre() {
        return genre;
    }

    public Optional<Status> getStatus() {
        return Optional.of(status);
    }

    public Optional<String> getThumbnailURL() {
        return Optional.of(thumbnailURL);
    }

    public Optional<Float> getScore() {
        return score >= 0 ? Optional.of(score) : Optional.empty();
    }

    @Override
    public String toString() {
        return "Manga{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", genre=" + genre +
                ", status=" + status +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", score=" + score +
                '}';
    }

    public static class Builder {

        private String url;
        private String title;
        private String artist;
        private String author;
        private String description;
        private List<String> genres = new ArrayList<>();
        private Status status;
        private String thumbnailURL;
        private float score;

        public Manga build() {
            if (url == null) {
                throw new BuilderException("URL is null");
            }
            if (title == null) {
                throw new BuilderException("Title is null");
            }

            return new Manga(url, title, artist, author, description, genres, status, thumbnailURL, score);
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

        public String getArtist() {
            return artist;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;
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
