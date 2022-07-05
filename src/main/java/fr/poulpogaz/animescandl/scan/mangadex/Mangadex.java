package fr.poulpogaz.animescandl.scan.mangadex;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Language;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.AbstractSimpleScanWebsite;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.SearchWebsite;
import fr.poulpogaz.animescandl.website.UnsupportedURLException;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.Select;
import fr.poulpogaz.animescandl.website.filter.url.ListTriStateCheckBox;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilter;
import fr.poulpogaz.animescandl.website.filter.url.UrlFilterList;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;
import java.util.*;

public class Mangadex extends AbstractSimpleScanWebsite
        implements SearchWebsite<Manga> {

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

        if (language != null && !manga.getLanguages().contains(language)) {
            throw new WebsiteException("The manga %s isn't translated in %s"
                    .formatted(manga.getTitle(), language));
        }

        return Collections.unmodifiableList(API.getChapters(manga, language));
    }

    @Override
    public Chapter getChapter(List<Chapter> allChapters, String url)
            throws IOException, InterruptedException, WebsiteException, JsonException {

        Optional<String> idOpt = Utils.getRegexGroupOrNull(url, "https://mangadex\\.org/chapter/(\\S*)/");

        if (idOpt.isPresent()) {
            String id = idOpt.get();

            for (Chapter c : allChapters) {
                if (c.getUrl().equals(id)) {
                    return c;
                }
            }
        }

        return null;
    }

    @Override
    protected PageIterator<String> createStringPageIterator(Chapter chapter)
            throws IOException, InterruptedException, WebsiteException, JsonException {
        return new StringPageIterator(chapter);
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

    @Override
    public boolean supportLanguage() {
        return true;
    }

    @Override
    public void selectLanguage(String language) {
        this.language = language;
    }

    @Override
    public FilterList getSearchFilter() {
        UrlFilterList.Builder builder = new UrlFilterList.Builder()
                .group("Demographic", "publicationDemographic[]")
                    .checkBox("Shounen", "shounen")
                    .checkBox("Shoujo", "shoujo")
                    .checkBox("Seinen", "seinen")
                    .checkBox("Josei", "none")
                    .build()
                .group("Content rating", "contentRating[]")
                    .checkBox("Erotica", "erotica")
                    .checkBox("Suggestive", "suggestive")
                    .checkBox("Safe", "safe")
                    .build()
                .group("Content rating", "status[]")
                    .checkBox("Ongoing", "ongoing")
                    .checkBox("Completed", "completed")
                    .checkBox("Hiatus", "hiatus")
                    .checkBox("Cancelled", "cancelled")
                    .build()
                .group("Format", "includedTags[]")
                    .addFilter(newTag("4-Koma", "b11fda93-8f1d-4bef-b2ed-8803d3733170"))
                    .addFilter(newTag("Adaptation", "f4122d1c-3b44-44d0-9936-ff7502c39ad3"))
                    .addFilter(newTag("Anthology", "51d83883-4103-437c-b4b1-731cb73d786c"))
                    .addFilter(newTag("Award Winning", "0a39b5a1-b235-4886-a747-1d05d216532d"))
                    .addFilter(newTag("Dounjinshi", "b13b2a48-c720-44a9-9c77-39c9979373fb"))
                    .addFilter(newTag("Fan Colored", "7b2ce280-79ef-4c09-9b58-12b7c23a9b78"))
                    .addFilter(newTag("Full Color", "f5ba408b-0e7a-484d-8d49-4e9125ac96de"))
                    .addFilter(newTag("Long Strip", "3e2b8dae-350e-4ab8-a8ce-016e844b9f0d"))
                    .addFilter(newTag("Official Colored", "320831a8-4026-470b-94f6-8353740e6f04"))
                    .addFilter(newTag("Oneshot", "0234a31e-a729-4e28-9d6a-3f87c4966b9e"))
                    .addFilter(newTag("User Created", "891cf039-b895-47f0-9229-bef4c96eccd4"))
                    .addFilter(newTag("Web Comic", "e197df38-d0e7-43b5-9b09-2842d0c326dd"))
                    .build()
                .group("Genres", "includedTags[]")
                    .addFilter(newTag("Action", "391b0423-d847-456f-aff0-8b0cfc03066b"))
                    .addFilter(newTag("Adventure", "87cc87cd-a395-47af-b27a-93258283bbc6"))
                    .addFilter(newTag("Boys' Love", "5920b825-4181-4a17-beeb-9918b0ff7a30"))
                    .addFilter(newTag("Comedy", "4d32cc48-9f00-4cca-9b5a-a839f0764984"))
                    .addFilter(newTag("Crime", "5ca48985-9a9d-4bd8-be29-80dc0303db72"))
                    .addFilter(newTag("Drama", "b9af3a63-f058-46de-a9a0-e0c13906197a"))
                    .addFilter(newTag("Fantasy", "cdc58593-87dd-415e-bbc0-2ec27bf404cc"))
                    .addFilter(newTag("Girls' Love", "a3c67850-4684-404e-9b7f-c69850ee5da6"))
                    .addFilter(newTag("Historical", "33771934-028e-4cb3-8744-691e866a923e"))
                    .addFilter(newTag("Horror", "cdad7e68-1419-41dd-bdce-27753074a640"))
                    .addFilter(newTag("Isekai", "ace04997-f6bd-436e-b261-779182193d3d"))
                    .addFilter(newTag("Magical Girls", "81c836c9-914a-4eca-981a-560dad663e73"))
                    .addFilter(newTag("Mecha", "50880a9d-5440-4732-9afb-8f457127e836"))
                    .addFilter(newTag("Medical", "c8cbe35b-1b2b-4a3f-9c37-db84c4514856"))
                    .addFilter(newTag("Mystery", "ee968100-4191-4968-93d3-f82d72be7e46"))
                    .addFilter(newTag("Philosophical", "b1e97889-25b4-4258-b28b-cd7f4d28ea9b"))
                    .addFilter(newTag("Psychological", "3b60b75c-a2d7-4860-ab56-05f391bb889c"))
                    .addFilter(newTag("Romance", "423e2eae-a7a2-4a8b-ac03-a8351462d71d"))
                    .addFilter(newTag("Sci-Fi", "256c8bd9-4904-4360-bf4f-508a76d67183"))
                    .addFilter(newTag("Slice of Life", "e5301a23-ebd9-49dd-a0cb-2add944c7fe9"))
                    .addFilter(newTag("Sports", "69964a64-2f90-4d33-beeb-f3ed2875eb4c"))
                    .addFilter(newTag("Superhero", "7064a261-a137-4d3a-8848-2d385de3a99c"))
                    .addFilter(newTag("Thriller", "07251805-a27e-4d59-b488-f0bfbec15168"))
                    .addFilter(newTag("Tragedy", "f8f62932-27da-4fe4-8ee1-6779a8c5edba"))
                    .addFilter(newTag("Wuxia", "acc803a4-c95a-4c22-86fc-eb6b582d82a2"))
                    .build()
                .group("Themes", "includedTags[]")
                    .addFilter(newTag("Aliens", "e64f6742-c834-471d-8d72-dd51fc02b835"))
                    .addFilter(newTag("Animals", "3de8c75d-8ee3-48ff-98ee-e20a65c86451"))
                    .addFilter(newTag("Cooking", "ea2bc92d-1c26-4930-9b7c-d5c0dc1b6869"))
                    .addFilter(newTag("Crossdressing", "9ab53f92-3eed-4e9b-903a-917c86035ee3"))
                    .addFilter(newTag("Delinquents", "da2d50ca-3018-4cc0-ac7a-6b7d472a29ea"))
                    .addFilter(newTag("Demons", "39730448-9a5f-48a2-85b0-a70db87b1233"))
                    .addFilter(newTag("Genderswap", "2bd2e8d0-f146-434a-9b51-fc9ff2c5fe6a"))
                    .addFilter(newTag("Ghosts", "3bb26d85-09d5-4d2e-880c-c34b974339e9"))
                    .addFilter(newTag("Gyaru", "fad12b5e-68ba-460e-b933-9ae8318f5b65"))
                    .addFilter(newTag("Harem", "aafb99c1-7f60-43fa-b75f-fc9502ce29c7"))
                    .addFilter(newTag("Incest", "5bd0e105-4481-44ca-b6e7-7544da56b1a3"))
                    .addFilter(newTag("Loli", "2d1f5d56-a1e5-4d0d-a961-2193588b08ec"))
                    .addFilter(newTag("Mafia", "85daba54-a71c-4554-8a28-9901a8b0afad"))
                    .addFilter(newTag("Magic", "a1f53773-c69a-4ce5-8cab-fffcd90b1565"))
                    .addFilter(newTag("Martial Arts", "799c202e-7daa-44eb-9cf7-8a3c0441531e"))
                    .addFilter(newTag("Military", "ac72833b-c4e9-4878-b9db-6c8a4a99444a"))
                    .addFilter(newTag("Monster Girls", "dd1f77c5-dea9-4e2b-97ae-224af09caf99"))
                    .addFilter(newTag("Monsters", "36fd93ea-e8b8-445e-b836-358f02b3d33d"))
                    .addFilter(newTag("Music", "f42fbf9e-188a-447b-9fdc-f19dc1e4d685"))
                    .addFilter(newTag("Ninja", "489dd859-9b61-4c37-af75-5b18e88daafc"))
                    .addFilter(newTag("Office Workers", "92d6d951-ca5e-429c-ac78-451071cbf064"))
                    .addFilter(newTag("Police", "df33b754-73a3-4c54-80e6-1a74a8058539"))
                    .addFilter(newTag("Post-Apocalyptic", "9467335a-1b83-4497-9231-765337a00b96"))
                    .addFilter(newTag("Reincarnation", "0bc90acb-ccc1-44ca-a34a-b9f3a73259d0"))
                    .addFilter(newTag("Reverse Harem", "65761a2a-415e-47f3-bef2-a9dababba7a6"))
                    .addFilter(newTag("Samurai", "81183756-1453-4c81-aa9e-f6e1b63be016"))
                    .addFilter(newTag("School Life", "caaa44eb-cd40-4177-b930-79d3ef2afe87"))
                    .addFilter(newTag("Shota", "ddefd648-5140-4e5f-ba18-4eca4071d19b"))
                    .addFilter(newTag("Supernatural", "eabc5b4c-6aff-42f3-b657-3e90cbd00b75"))
                    .addFilter(newTag("Survival", "5fff9cde-849c-4d78-aab0-0d52b2ee1d25"))
                    .addFilter(newTag("Time Travel", "292e862b-2d17-4062-90a2-0356caa4ae27"))
                    .addFilter(newTag("Traditional Games", "31932a7e-5b8e-49a6-9f12-2afa39dc544c"))
                    .addFilter(newTag("Vampires", "d7d1730f-6eb0-4ba6-9437-602cac38664c"))
                    .addFilter(newTag("Video Games", "9438db5a-7e2a-4ac0-b39e-e0d95a34b8a8"))
                    .addFilter(newTag("Villainess", "d14322ac-4d6f-4e9b-afd9-629d5f4d8a41"))
                    .addFilter(newTag("Virtual Reality", "8c86611e-fab7-4986-9dec-d1a2f44acdd5"))
                    .addFilter(newTag("Zombies", "631ef465-9aba-4afb-b0fc-ea10efe274a8"))
                    .build()
                .group("Content")
                    .addFilter(newTag("Gore", "b29d6a3d-1569-4e7a-8caf-7557bc92cd5d"))
                    .addFilter(newTag("Sexual Violence", "97893a4c-12af-4dac-b6be-0dffb353568e"))
                    .build()
                .addFilter(new SortFilter());

        UrlFilterList.GroupBuilder group = builder.group("Original Language", "originalLanguage[]");

        for (Language l : Language.values()) {
            group.checkBox(l.getName(), l.getCode());
        }

        return group.build().build();
    }

    private ListTriStateCheckBox newTag(String name, String id) {
        return new ListTriStateCheckBox(name, "includedTags[]", "excludedTags[]", id);
    }

    @Override
    public List<Manga> search(String search, FilterList filter)
            throws JsonException, WebsiteException, IOException, InterruptedException {
        if (filter instanceof UrlFilterList list) {
            return API.search(search, list);
        }

        return List.of();
    }

    private static class SortFilter extends Select<String> implements UrlFilter {

        public SortFilter() {
            super("Sort By", Sort.names);
        }

        @Override
        public String getQueryName() {
            if (value == null) {
                return null;
            }

            String query = Sort.values()[value / 2].getQuery();

            return "order[" + query + "]";
        }

        @Override
        public String getQueryArgument() {
            if (value == null) {
                return null;
            }

            if (value % 2 == 0) {
                return "asc";
            } else {
                return "desc";
            }
        }
    }

    private enum Sort {
        RELEVANCE("Worst Match",     "Best Match",       "relevance"),
        UPLOAD   ("Latest Upload",   "Oldest Upload",    "latestUploadedChapter"),
        TITLE    ("Title Ascending", "Title Descending", "title"),
        RATING   ("Lowest Rating",   "Highest Rating",    "rating"),
        FOLLOWS  ("Fewest Follows",  "Most Follows",   "followedCount"),
        ADDED    ("Oldest Added",    "Recently Added",     "createdAt"),
        YEAR     ("Year Ascending",  "Year Descending",  "year");

        private static final List<String> names =
                Arrays.stream(Sort.values())
                        .<String>mapMulti((sort, consumer) -> {
                            consumer.accept(sort.getNameAscending());
                            consumer.accept(sort.getNameAscending());
                        })
                        .toList();

        private final String nameAscending;
        private final String nameDescending;
        private final String query;

        Sort(String nameAscending, String nameDescending, String query) {
            this.nameAscending = nameAscending;
            this.nameDescending = nameDescending;
            this.query = query;
        }

        public String getNameAscending() {
            return nameAscending;
        }

        public String getNameDescending() {
            return nameDescending;
        }

        public String getQuery() {
            return query;
        }
    }
}
