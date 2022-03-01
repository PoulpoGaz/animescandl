package fr.poulpogaz.animescandl;

public record Video(String file, String subtile, int quality, String format) {

    public static final String UNKNOWN = "unknown";
    public static final String M3U8 = "m3u8";

    public Video(String file, int quality, String format) {
        this(file, null, quality, format);
    }

    public Video(String file, int quality) {
        this(file, null, quality, UNKNOWN);
    }
}