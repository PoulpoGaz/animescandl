package fr.poulpogaz.animescandl.scan.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.scan.AbstractSimpleScanWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Mangadex extends AbstractSimpleScanWebsite<Manga, Chapter> {

    private String language;

    @Override
    public String name() {
        return "Mangadex";
    }

    @Override
    public String url() {
        return "https://mangadex.org";
    }

    @Override
    public String version() {
        return "dev1";
    }

    @Override
    public boolean isChapterURL(String url) {
        return url.startsWith("https://mangadex.org/chapter/");
    }

    @Override
    public boolean isMangaURL(String url) {
        return url.startsWith("https://mangadex.org/title/");
    }

    @Override
    public Manga getManga(String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {

        if (isChapterURL(url)) {
            String id = Utils.getRegexGroup(url, "chapter/(.*)(?:/.*)");
            return API.getMangaFromChapterID(id);
        } else if (isMangaURL(url)) {
            String id = Utils.getRegexGroup(url, "title/(.*)(?:/.*)");
            return API.getMangaFromMangaID(id);
        } else {
            throw new UnsupportedURLException(this, url);
        }
    }

    @Override
    public List<Chapter> getChapters(Manga manga)
            throws IOException, InterruptedException, WebsiteException, JsonException {

        if (language != null && manga.getLanguages().contains(language)) {
            throw new WebsiteException("The manga %s isn't translated in %s"
                    .formatted(manga.getTitle(), language));
        }

        return Collections.unmodifiableList(API.getChapters(manga, language));
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        return new StringPageIterator(chapter);
    }

    @Override
    public boolean supportLanguage() {
        return true;
    }

    @Override
    public void selectLanguage(String language) {
        this.language = language;
    }

    @Override
    public List<Manga> search() {
        return null;
    }


    private static class StringPageIterator implements PageIterator<String> {

        private final List<String> pages;
        private int index = 0;

        public StringPageIterator(Chapter chapter)
                throws IOException, InterruptedException, WebsiteException, JsonException {
            pages = API.getPages(chapter);
        }

        @Override
        public boolean hasNext() {
            return index < pages.size();
        }

        @Override
        public String next() throws WebsiteException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            String page = pages.get(index);
            index++;

            return page;
        }

        @Override
        public Optional<Integer> nPages() {
            return Optional.of(pages.size());
        }
    }
}
