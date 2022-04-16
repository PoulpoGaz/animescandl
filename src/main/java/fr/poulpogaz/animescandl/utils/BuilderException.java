package fr.poulpogaz.animescandl.utils;

public class BuilderException extends IllegalStateException {

    public BuilderException() {
    }

    public BuilderException(String s) {
        super(s);
    }

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }
}
