package fr.poulpogaz.animescandl.utils.math;

/**
 * Minimalist set library.
 * Possible sets are:
 *  -singleton
 *  -interval
 *  -union of singleton or interval
 */
public interface Set {

    boolean contains(float real);

    Set union(Set set);

    Set intersect(Set set);
}
