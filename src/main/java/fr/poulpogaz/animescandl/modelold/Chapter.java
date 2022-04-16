package fr.poulpogaz.animescandl.modelold;

public class Chapter extends DefaultEntry {

    protected final String manga;
    protected final String volume;
    protected final String chapter;

    public Chapter(String url, int index, String manga, String volume, String chapter) {
        super(url, index);
        this.manga = manga;
        this.volume = volume;
        this.chapter = chapter;
    }

    public String getChapterPDFName() {
        StringBuilder builder = new StringBuilder();

        if (manga != null) {
            builder.append(manga);

            if (volume != null || chapter != null) {
                builder.append(" - ");
            }
        }
        if (volume != null) {
            builder.append(volume);

            if (chapter != null) {
                builder.append(" - ");
            }
        }
        if (chapter != null) {
            builder.append(chapter);
        }

        if (builder.isEmpty()) {
            return String.valueOf(index);
        }

        return builder.toString();
    }

    public String manga() {
        return manga;
    }

    public String volume() {
        return volume;
    }

    public String chapter() {
        return chapter;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "manga='" + manga + '\'' +
                ", volume='" + volume + '\'' +
                ", chapter='" + chapter + '\'' +
                ", url='" + url + '\'' +
                ", index=" + index +
                '}';
    }
}
