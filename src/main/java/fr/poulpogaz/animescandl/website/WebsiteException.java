package fr.poulpogaz.animescandl.website;

public class WebsiteException extends Exception {

    public WebsiteException() {
        super();
    }

    public WebsiteException(String message) {
        super(message);
    }

    public WebsiteException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebsiteException(Throwable cause) {
        super(cause);
    }
}
