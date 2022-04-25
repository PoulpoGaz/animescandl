package fr.poulpogaz.animescandl.model;

import java.util.Objects;
import java.util.Optional;

public class Source {

    private final String url;
    private final String subtitleURL;
    private final int quality;
    private final String videoFormat;

    public Source(String url, String subtitleURL, int quality, String videoFormat) {
        this.url = Objects.requireNonNull(url);
        this.subtitleURL = subtitleURL;
        this.quality = quality;
        this.videoFormat = videoFormat;
    }

    public String getUrl() {
        return url;
    }

    public Optional<String> getSubtitleURL() {
        return Optional.ofNullable(subtitleURL);
    }

    public Optional<Integer> getQuality() {
        return Optional.of(quality);
    }

    public Optional<String> getVideoFormat() {
        return Optional.ofNullable(videoFormat);
    }
}
