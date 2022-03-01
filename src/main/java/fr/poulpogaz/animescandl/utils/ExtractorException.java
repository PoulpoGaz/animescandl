package fr.poulpogaz.animescandl.utils;

public class ExtractorException extends Exception {

    public ExtractorException() {
        super();
    }

    public ExtractorException(String message) {
        super(message);
    }

    public ExtractorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtractorException(Throwable cause) {
        super(cause);
    }
}
