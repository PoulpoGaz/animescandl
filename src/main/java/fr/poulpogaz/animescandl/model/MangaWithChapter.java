package fr.poulpogaz.animescandl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MangaWithChapter extends Manga {

    private final List<Chapter> chapters;

    public MangaWithChapter(String url, String title, List<Chapter> chapters) {
        super(url, title);
        this.chapters = chapters;
    }

    public MangaWithChapter(String url,
                            String title,
                            String artist,
                            String author,
                            String description,
                            List<String> genre,
                            Status status,
                            List<String> languages,
                            String thumbnailURL,
                            float score,
                            List<Chapter> chapters) {
        super(url, title, artist, author, description, genre, status, languages, thumbnailURL, score);
        this.chapters = Collections.unmodifiableList(chapters);
    }

    private MangaWithChapter(MangaWithChapter.Builder builder) {
        super(builder.getUrl(),
                builder.getTitle(),
                builder.getArtist(),
                builder.getAuthor(),
                builder.getDescription(),
                builder.getGenres(),
                builder.getStatus(),
                builder.getLanguage(),
                builder.getThumbnailURL(),
                builder.getScore());

        this.chapters = builder.getChapters().stream()
                .map((c) -> c.setManga(this).build())
                .toList();
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public static class Builder extends MangaBuilderBase<MangaWithChapter> {

        private List<Chapter.Builder> chapters = new ArrayList<>();

        @Override
        protected MangaWithChapter buildImpl() {
            return new MangaWithChapter(this);
        }

        public List<Chapter.Builder> getChapters() {
            return chapters;
        }

        public Builder setChapters(List<Chapter.Builder> chapters) {
            this.chapters = chapters;
            return this;
        }

        public Builder addChapter(Chapter.Builder chapter) {
            chapters.add(chapter);

            return this;
        }
    }
}
