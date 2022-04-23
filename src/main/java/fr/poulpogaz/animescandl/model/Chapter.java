package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.Objects;
import java.util.Optional;

public class Chapter {

    public static final float UNKNOWN_CHAPTER = -100;

    public static final float UNKNOWN_VOLUME = -100;
    public static final float NONE_VOLUME = -200;

    private final Manga manga;

    private final String url;
    private final float chapterNumber;

    private final String name;
    private final float volume;

    public Chapter(Manga manga, String url, float chapterNumber) {
        this(manga, url, chapterNumber, null, UNKNOWN_VOLUME);
    }

    public Chapter(Manga manga, String url, float chapterNumber, String name) {
        this(manga, url, chapterNumber, name, UNKNOWN_VOLUME);
    }

    public Chapter(Manga manga, String url, float chapterNumber, String name, float volume) {
        this.manga = Objects.requireNonNull(manga);
        this.url = Objects.requireNonNull(url);
        this.chapterNumber = chapterNumber;
        this.name = name;
        this.volume = volume;
    }

    public Manga getManga() {
        return manga;
    }

    public String getUrl() {
        return url;
    }

    public float getChapterNumber() {
        return chapterNumber;
    }

    public Optional<String> getName() {
        return Optional.of(name);
    }

    public Optional<Float> getVolume() {
        if (volume == UNKNOWN_VOLUME) {
            return Optional.empty();
        } else {
            return Optional.of(volume);
        }
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "manga=" + manga +
                ", url='" + url + '\'' +
                ", chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                ", volume=" + volume +
                '}';
    }

    public static class Builder {

        private Manga manga;

        private String url;
        private float chapterNumber = UNKNOWN_CHAPTER;

        private String name;
        private float volume = UNKNOWN_VOLUME;

        public Chapter build() {
            if (url == null) {
                throw new BuilderException("URL is null");
            }

            if (manga == null) {
                throw new BuilderException("Manga is null");
            }

            return new Chapter(manga, url, chapterNumber, name, volume);
        }

        public Manga getManga() {
            return manga;
        }

        public Builder setManga(Manga manga) {
            this.manga = manga;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public float getChapterNumber() {
            return chapterNumber;
        }

        public Builder setChapterNumber(float chapterNumber) {
            this.chapterNumber = chapterNumber;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public float getVolume() {
            return volume;
        }

        public Builder setVolume(float volume) {
            this.volume = volume;
            return this;
        }
    }
}
