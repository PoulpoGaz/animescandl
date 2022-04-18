package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.ArrayList;
import java.util.List;

public abstract class MangaBuilderBase<M extends Manga> {
    
    protected String url;
    protected String title;
    protected String artist;
    protected String author;
    protected String description;
    protected List<String> genres = new ArrayList<>();
    protected Status status;
    protected List<String> languages = new ArrayList<>();
    protected String thumbnailURL;
    protected float score = -1;

    public M build() {
        if (url == null) {
            throw new BuilderException("URL is null");
        }
        if (title == null) {
            throw new BuilderException("Title is null");
        }

        return buildImpl();
    }
    
    protected abstract M buildImpl();

    public String getUrl() {
        return url;
    }

    public MangaBuilderBase<M> setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MangaBuilderBase<M> setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public MangaBuilderBase<M> setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public MangaBuilderBase<M> setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MangaBuilderBase<M> setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<String> getGenres() {
        return genres;
    }

    public MangaBuilderBase<M> setGenres(List<String> genres) {
        this.genres = genres;
        return this;
    }

    public MangaBuilderBase<M> addGenre(String genre) {
        genres.add(genre);

        return this;
    }

    public Status getStatus() {
        return status;
    }

    public MangaBuilderBase<M> setStatus(Status status) {
        this.status = status;
        return this;
    }

    public List<String> getLanguage() {
        return languages;
    }

    public MangaBuilderBase<M> setLanguages(List<String> languages) {
        this.languages = languages;
        return this;
    }

    public MangaBuilderBase<M> addLanguage(String language) {
        languages.add(language);

        return this;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public MangaBuilderBase<M> setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
        return this;
    }

    public float getScore() {
        return score;
    }

    public MangaBuilderBase<M> setScore(float score) {
        this.score = score;
        return this;
    }
}
