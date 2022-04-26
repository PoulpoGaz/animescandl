package fr.poulpogaz.animescandl.utils.log;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.Supplier;

public class ASDLLogger extends Logger {

    protected ASDLLogger(LoggerContext context, String name, MessageFactory messageFactory) {
        super(context, name, messageFactory);
    }

    public void debugln(CharSequence message) {
        super.debug(message + System.lineSeparator());
    }

    public void debugln(CharSequence message, Throwable throwable) {
        super.debug(message + System.lineSeparator(), throwable);
    }

    public void debugln(Object message) {
        super.debug(message + System.lineSeparator());
    }

    public void debugln(Object message, Throwable throwable) {
        super.debug(message + System.lineSeparator(), throwable);
    }

    public void debugln(String message) {
        super.debug(message + System.lineSeparator());
    }

    public void debugln(String message, Object... params) {
        super.debug(message + System.lineSeparator(), params);
    }

    public void debugln(String message, Throwable throwable) {
        super.debug(message + System.lineSeparator(), throwable);
    }

    public void debugln(String message, Supplier<?>... paramSuppliers) {
        super.debug(message + System.lineSeparator(), paramSuppliers);
    }

    public void debugln(String message, Object p0) {
        super.debug(message + System.lineSeparator(), p0);
    }

    public void debugln(String message, Object p0, Object p1) {
        super.debug(message + System.lineSeparator(), p0, p1);
    }

    public void debugln(String message, Object p0, Object p1, Object p2) {
        super.debug(message + System.lineSeparator(), p0, p1, p2);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void debugln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.debug(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // ERROR
    public void errorln(CharSequence message) {
        super.error(message + System.lineSeparator());
    }

    public void errorln(CharSequence message, Throwable throwable) {
        super.error(message + System.lineSeparator(), throwable);
    }

    public void errorln(Object message) {
        super.error(message + System.lineSeparator());
    }

    public void errorln(Object message, Throwable throwable) {
        super.error(message + System.lineSeparator(), throwable);
    }

    public void errorln(String message) {
        super.error(message + System.lineSeparator());
    }

    public void errorln(String message, Object... params) {
        super.error(message + System.lineSeparator(), params);
    }

    public void errorln(String message, Throwable throwable) {
        super.error(message + System.lineSeparator(), throwable);
    }

    public void errorln(String message, Supplier<?>... paramSuppliers) {
        super.error(message + System.lineSeparator(), paramSuppliers);
    }

    public void errorln(String message, Object p0) {
        super.error(message + System.lineSeparator(), p0);
    }

    public void errorln(String message, Object p0, Object p1) {
        super.error(message + System.lineSeparator(), p0, p1);
    }

    public void errorln(String message, Object p0, Object p1, Object p2) {
        super.error(message + System.lineSeparator(), p0, p1, p2);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void errorln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.error(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // FATAL
    public void fatalln(CharSequence message) {
        super.fatal(message + System.lineSeparator());
    }

    public void fatalln(CharSequence message, Throwable throwable) {
        super.fatal(message + System.lineSeparator(), throwable);
    }

    public void fatalln(Object message) {
        super.fatal(message + System.lineSeparator());
    }

    public void fatalln(Object message, Throwable throwable) {
        super.fatal(message + System.lineSeparator(), throwable);
    }

    public void fatalln(String message) {
        super.fatal(message + System.lineSeparator());
    }

    public void fatalln(String message, Object... params) {
        super.fatal(message + System.lineSeparator(), params);
    }

    public void fatalln(String message, Throwable throwable) {
        super.fatal(message + System.lineSeparator(), throwable);
    }

    public void fatalln(String message, Supplier<?>... paramSuppliers) {
        super.fatal(message + System.lineSeparator(), paramSuppliers);
    }

    public void fatalln(String message, Object p0) {
        super.fatal(message + System.lineSeparator(), p0);
    }

    public void fatalln(String message, Object p0, Object p1) {
        super.fatal(message + System.lineSeparator(), p0, p1);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void fatalln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.fatal(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // INFO

    public void infoln(CharSequence message) {
        super.info(message + System.lineSeparator());
    }

    public void infoln(CharSequence message, Throwable throwable) {
        super.info(message + System.lineSeparator(), throwable);
    }

    public void infoln(Object message) {
        super.info(message + System.lineSeparator());
    }

    public void infoln(Object message, Throwable throwable) {
        super.info(message + System.lineSeparator(), throwable);
    }

    public void infoln(String message) {
        super.info(message + System.lineSeparator());
    }

    public void infoln(String message, Object... params) {
        super.info(message + System.lineSeparator(), params);
    }

    public void infoln(String message, Throwable throwable) {
        super.info(message + System.lineSeparator(), throwable);
    }

    public void infoln(String message, Supplier<?>... paramSuppliers) {
        super.info(message + System.lineSeparator(), paramSuppliers);
    }

    public void infoln(String message, Object p0) {
        super.info(message + System.lineSeparator(), p0);
    }

    public void infoln(String message, Object p0, Object p1) {
        super.info(message + System.lineSeparator(), p0, p1);
    }

    public void infoln(String message, Object p0, Object p1, Object p2) {
        super.info(message + System.lineSeparator(), p0, p1, p2);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void infoln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.info(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // TRACE
    public void traceln(CharSequence message) {
        super.trace(message + System.lineSeparator());
    }

    public void traceln(CharSequence message, Throwable throwable) {
        super.trace(message + System.lineSeparator(), throwable);
    }

    public void traceln(Object message) {
        super.trace(message + System.lineSeparator());
    }

    public void traceln(Object message, Throwable throwable) {
        super.trace(message + System.lineSeparator(), throwable);
    }

    public void traceln(String message) {
        super.trace(message + System.lineSeparator());
    }

    public void traceln(String message, Object... params) {
        super.trace(message + System.lineSeparator(), params);
    }

    public void traceln(String message, Throwable throwable) {
        super.trace(message + System.lineSeparator(), throwable);
    }

    public void traceln(String message, Supplier<?>... paramSuppliers) {
        super.trace(message + System.lineSeparator(), paramSuppliers);
    }

    public void traceln(String message, Object p0) {
        super.trace(message + System.lineSeparator(), p0);
    }

    public void traceln(String message, Object p0, Object p1) {
        super.trace(message + System.lineSeparator(), p0, p1);
    }

    public void traceln(String message, Object p0, Object p1, Object p2) {
        super.trace(message + System.lineSeparator(), p0, p1, p2);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void traceln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.trace(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    // WARN
    public void warnln(CharSequence message) {
        super.warn(message + System.lineSeparator());
    }

    public void warnln(CharSequence message, Throwable throwable) {
        super.warn(message + System.lineSeparator(), throwable);
    }

    public void warnln(Object message) {
        super.warn(message + System.lineSeparator());
    }

    public void warnln(Object message, Throwable throwable) {
        super.warn(message + System.lineSeparator(), throwable);
    }

    public void warnln(String message) {
        super.warn(message + System.lineSeparator());
    }

    public void warnln(String message, Object... params) {
        super.warn(message + System.lineSeparator(), params);
    }

    public void warnln(String message, Throwable throwable) {
        super.warn(message + System.lineSeparator(), throwable);
    }

    public void warnln(String message, Supplier<?>... paramSuppliers) {
        super.warn(message + System.lineSeparator(), paramSuppliers);
    }

    public void warnln(String message, Object p0) {
        super.warn(message + System.lineSeparator(), p0);
    }

    public void warnln(String message, Object p0, Object p1) {
        super.warn(message + System.lineSeparator(), p0, p1);
    }

    public void warnln(String message, Object p0, Object p1, Object p2) {
        super.warn(message + System.lineSeparator(), p0, p1, p2);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public void warnln(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        super.warn(message + System.lineSeparator(), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }
}
