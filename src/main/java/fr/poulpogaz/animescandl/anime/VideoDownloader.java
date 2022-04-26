package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.Episode;
import fr.poulpogaz.animescandl.model.Source;
import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Just redirect to M3U8Downloader because there is only
 * nekosama right now
 */
public class VideoDownloader {

    public static void download(Episode episode, Source source, Path out) throws WebsiteException, IOException {
        String format = source.getVideoFormat().orElse(null);

        if ("m3u8".equals(format)) {
            M3U8Downloader.download(source, out.toString());
        } else {
            throw new WebsiteException("Unsupported format: " + format);
        }
    }
}
