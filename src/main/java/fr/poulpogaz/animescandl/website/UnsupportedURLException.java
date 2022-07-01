package fr.poulpogaz.animescandl.website;

public class UnsupportedURLException extends WebsiteException {

    public UnsupportedURLException(Website w, String url, String... validURL) {
        super(createErrorMessage(w, url, validURL));
    }

    private static String createErrorMessage(Website w, String url, String[] supportedURLs) {
        StringBuilder b = new StringBuilder();
        b.append(w.name()).append(" doesn't support this url: ");
        b.append(url);
        b.append(". Supported urls are: ");

        for (int i = 0; i < supportedURLs.length - 1; i++) {
            String supported = supportedURLs[i];
            b.append(supported).append(", ");
        }

        if (supportedURLs.length > 0) {
            b.append(supportedURLs[supportedURLs.length - 1]);
        }

        return b.toString();
    }
}
