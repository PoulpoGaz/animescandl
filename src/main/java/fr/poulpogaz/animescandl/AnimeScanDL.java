package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.anime.AnimeWebsite;
import fr.poulpogaz.animescandl.anime.Nekosama;
import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.model.Manga;
import fr.poulpogaz.animescandl.scan.Japanread;
import fr.poulpogaz.animescandl.scan.MangaRead;
import fr.poulpogaz.animescandl.scan.ScanWebsite;
import fr.poulpogaz.animescandl.scan.SushiScan;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.scan.japscan.Japscan;
import fr.poulpogaz.animescandl.scan.mangadex.Mangadex;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.utils.ScanWriter;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.website.Website;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnimeScanDL {

    public static AnimeScanDL createDefault() throws IOException {
        AnimeScanDL a = new AnimeScanDL();
        a.addWebsite(new SushiScan());
        a.addWebsite(new Japanread());
        a.addWebsite(new MangaRead());
        a.addWebsite(new Mangadex());
        a.addWebsite(new Japscan());

        a.addWebsite(new Nekosama());

        return a;
    }

    private List<Website> websites = new ArrayList<>();

    public AnimeScanDL() {

    }

    public void process(String url, Settings settings)
            throws WebsiteException, JsonException, IOException, InterruptedException {
        if (!HttpUtils.isValidURL(url)) {
            throw new WebsiteException("Not an url: " + url);
        }

        Website w = getWebsiteFor(url);
        if (w instanceof ScanWebsite<?,?> s) {
            downloadScan(s, url, settings);
        } else if (w instanceof AnimeWebsite<?,?>) {

        } else {
            throw new IllegalStateException("Unknown website type");
        }
    }

    protected Website getWebsiteFor(String url) throws WebsiteException {
        for (Website w : websites) {
            if (w.isSupported(url)) {
                return w;
            }
        }

        throw new WebsiteException("No website found for " + url);
    }

    protected <M extends Manga, C extends Chapter>
    void downloadScan(ScanWebsite<M, C> s, String url, Settings settings)
            throws IOException, WebsiteException, InterruptedException, JsonException {
        M manga = s.getManga(url);
        List<C> chapters = s.getChapters(manga);

        IRequestSender sender = asRequestSender(s);
        ScanWriter sw = new ScanWriter(manga.getTitle(), settings.concatenateAll(), settings.out());

        for (C chap : chapters) {
            sw.newScan(chap.getName().orElse(manga.getTitle() + " - " + chap.getChapterNumber()));

            if (Utils.contains(s.supportedIterators(), String.class)) {
                PageIterator<String> iterator = s.getPageIterator(chap, String.class);

                while (iterator.hasNext()) {
                    sw.addPage(sender, iterator.next());
                }
            } else if (Utils.contains(s.supportedIterators(), InputStream.class)) {
                PageIterator<InputStream> iterator = s.getPageIterator(chap, InputStream.class);

                while (iterator.hasNext()) {
                    sw.addPage(iterator.next());
                }
            } else if (Utils.contains(s.supportedIterators(), BufferedImage.class)) {
                PageIterator<BufferedImage> iterator = s.getPageIterator(chap, BufferedImage.class);

                while (iterator.hasNext()) {
                    sw.addPage(iterator.next());
                }
            }

            sw.endScan();
        }

        sw.endAll();
    }

    protected IRequestSender asRequestSender(Website w) {
        if (w instanceof IRequestSender s) {
            return s;
        } else {
            return HttpUtils.STANDARD;
        }
    }

    public void addWebsite(Website website) {
        websites.add(Objects.requireNonNull(website));
    }

    public List<Website> getWebsites() {
        return websites;
    }
}
