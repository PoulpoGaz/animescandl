package fr.poulpogaz.animescandl.website;

public class DOMException extends WebsiteException {

    public DOMException() {
    }

    public DOMException(String message) {
        super(message);
    }

    public DOMException(String message, Throwable cause) {
        super(message, cause);
    }

    public DOMException(Throwable cause) {
        super(cause);
    }
}
