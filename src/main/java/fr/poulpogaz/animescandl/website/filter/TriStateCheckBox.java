package fr.poulpogaz.animescandl.website.filter;

import fr.poulpogaz.json.tree.JsonElement;

public class TriStateCheckBox extends Filter<Integer> {

    public static final int UNSELECTED = 0;
    public static final int SELECTED = 1;
    public static final int EXCLUDE = 2;

    public TriStateCheckBox(String name) {
        super(name);
    }

    @Override
    public void setValue(JsonElement value) throws InvalidValueException {
        int v;

        if (value.isBoolean() || value.isNumber() || value.isString()) {
            v = value.optionalInt().orElse(-1);

        } else {
            throw new InvalidValueException(name + ": Expecting boolean or int or string");
        }

        if (v < 0 || v >= 2) {
            throw new InvalidValueException(name + ": Invalid value: " + value.getAsString());
        }

        this.value = v;
    }

    @Override
    public void setValue(Integer value) {
        if (value >= UNSELECTED && value <= EXCLUDE) {
            this.value = value;
        }
    }

    public void nextState() {
        value = (value + 1) % 3;
    }

    public void unselect() {
        value = UNSELECTED;
    }

    public void select() {
        value = SELECTED;
    }

    public void exclude() {
        value = EXCLUDE;
    }
}
