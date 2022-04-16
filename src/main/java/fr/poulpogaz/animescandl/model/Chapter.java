package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;

import java.util.Optional;

public class Chapter {

    public static final float UNKNOWN_CHAPTER = -100;
    public static final float NONE_CHAPTER = -200;

    private final String url;
    private final float chapterNumber;

    private final String name;
    private final float volume;

    public Chapter(String url, float chapterNumber) {
        this(url, chapterNumber, null, UNKNOWN_CHAPTER);
    }

    public Chapter(String url, float chapterNumber, String name) {
        this(url, chapterNumber, name, UNKNOWN_CHAPTER);
    }

    public Chapter(String url, float chapterNumber, String name, float volume) {
        this.url = url;
        this.chapterNumber = chapterNumber;
        this.name = name;
        this.volume = volume;
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
        if (volume == Integer.MIN_VALUE) {
            return Optional.empty();
        } else {
            return Optional.of(volume);
        }
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "url='" + url + '\'' +
                ", chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                ", volume=" + volume +
                '}';
    }

    public static class Builder {
        private String url;
        private float chapterNumber;

        private String name;
        private float volume = Integer.MIN_VALUE;

        public Chapter build() {
            if (url == null) {
                throw new BuilderException("URL is null");
            }

            return new Chapter(url, chapterNumber, name, volume);
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
