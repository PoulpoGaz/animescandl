package fr.poulpogaz.animescandl.website;

import fr.poulpogaz.animescandl.*;
import fr.poulpogaz.animescandl.model.Entry;
import fr.poulpogaz.animescandl.model.Title;
import fr.poulpogaz.animescandl.utils.M3U8Downloader;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class AbstractVideoWebsite<E extends Entry, T extends Title> extends AbstractWebsite<E, T> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractVideoWebsite.class);

    protected abstract String getFileName(String url);

    protected abstract List<Video> extract(String url) throws Throwable;

    @Override
    protected void processEntry(E entry, Settings settings) {
        LOGGER.info("Downloading webpage {}", entry.url());

        List<Video> videos;
        try {
            videos = extract(entry.url());
        } catch (Throwable e) {
            LOGGER.warn("Can't extract videos", e);
            return;
        }

        if (videos == null || videos.isEmpty()) {
            LOGGER.warn("Website not supported or no videos found");
            return;
        }

        videos.sort(Comparator.comparingInt(Video::quality).reversed());

        LOGGER.info("Video(s) found:");
        videos.forEach((v) -> LOGGER.info("{}p - {}", v.quality(), v.file()));

        if (NO_DOWNLOAD) {
            return;
        }

        for (Video video : videos) {
            try {
                String dst = getFileName(entry.url()) + ".mp4";

                if (settings.out() != null) {
                    dst = settings.out().resolve(dst).toString();
                }

                M3U8Downloader.download(video, dst);
                LOGGER.info("Download finished!");

                return;
            } catch (IOException e) {
                LOGGER.warn("Failed to download video: {}", video, e);
            }
        }

        LOGGER.warn("No video has been downloaded");
    }
}