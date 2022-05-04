package fr.poulpogaz.animescandl.website.filter;

public class CheckBox extends Filter<Boolean> {

    public CheckBox(String name) {
        super(name, false);
    }

    public void invert() {
        value = !value;
    }

    public void select() {
        value = true;
    }

    public void unselect() {
        value = false;
    }
}