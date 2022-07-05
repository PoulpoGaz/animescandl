package fr.poulpogaz.animescandl.scan.writers;

import java.io.IOException;

@FunctionalInterface
public interface AddPage<T> {

    void addPage(T page) throws IOException, InterruptedException;
}