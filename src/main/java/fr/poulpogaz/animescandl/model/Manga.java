package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.website.ScanWebsite;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@code Manga} is loaded wia the {@link ScanWebsite#getManga(String)}
 * function. The function should be optimized to limit the number
 * of request.
 *
 * It is preferred that a {@code Manga}
 * doesn't store any reference to a {@code Chapter}
 * The preferred way to load a {@code Chapter} is to
 * use {@link ScanWebsite#getChapters(Manga)}
 *
 * However, some websites may use, for performance
 * or because it's impossible to do in another way,
 * the {@link MangaWithChapter} object.
 */
public class Manga {

    private final String url;
    private final String title;
    private final String artist;
    private final String author;
    private final String description;
    private final List<String> genres;
    private final Status status;
    private final List<String> languages;
    private final String thumbnailURL;
    private final float score;

    public Manga(String url, String title) {
        this(url, title, null, null, null, null, null, null, null, -1);
    }

    public Manga(String url,
                 String title,
                 String artist,
                 String author,
                 String description,
                 List<String> genres,
                 Status status,
                 List<String> languages,
                 String thumbnailURL,
                 float score) {
        this.url = Objects.requireNonNull(url);
        this.title = Objects.requireNonNull(title);
        this.artist = artist;
        this.author = author;
        this.description = description;
        this.genres = genres == null ? List.of() : Collections.unmodifiableList(genres);
        this.status = status;
        this.languages = languages == null ? List.of() : Collections.unmodifiableList(languages);
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

    public List<String> getGenres() {
        return genres;
    }

    public Optional<Status> getStatus() {
        return Optional.of(status);
    }

    public List<String> getLanguages() {
        return languages;
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
                ", genre=" + genres +
                ", status=" + status +
                ", languages=" + languages +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", score=" + score +
                '}';
    }

    public static class Builder extends MangaBuilderBase<Manga> {

        @Override
        public Manga buildImpl() {
            return new Manga(url, title, artist, author, description, genres, status, languages, thumbnailURL, score);
        }
    }
}
