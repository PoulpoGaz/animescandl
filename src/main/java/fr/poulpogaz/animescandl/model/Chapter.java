package fr.poulpogaz.animescandl.model;

import fr.poulpogaz.animescandl.utils.BuilderException;
import org.checkerframework.checker.units.qual.C;

import java.util.Optional;

public class Chapter {

    private final String url;
    private final float chapterNumber;

    private final String name;

    public Chapter(String url, float chapterNumber) {
        this(url, chapterNumber, null);
    }

    public Chapter(String url, float chapterNumber, String name) {
        this.url = url;
        this.chapterNumber = chapterNumber;
        this.name = name;
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

    @Override
    public String toString() {
        return "Chapter{" +
                "url='" + url + '\'' +
                ", chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Builder {
        private String url;
        private float chapterNumber;

        private String name;
        private float volume;

        public Chapter build() {
            if (url == null) {
                throw new BuilderException("URL is null");
            }

            return new Chapter(url, chapterNumber, name);
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
    }
}
