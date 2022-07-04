package fr.poulpogaz.animescandl.website.filter;

import fr.poulpogaz.json.tree.JsonElement;

public class CheckBox extends Filter<Boolean> {

    public CheckBox(String name) {
        super(name, false);
    }

    @Override
    public void setValue(JsonElement value) throws InvalidValueException {
        if (value.isBoolean()) {
            setValue(value.getAsBoolean());
        } else if (value.isNumber()) {
            setValue(value.getAsInt() != 0);
        } else if (value.isString()) {
            setValue(!value.getAsString().equals("0"));
        } else {
            throw new InvalidValueException("Expecting boolean or int or string for " + name);
        }
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

    public boolean isSelected() {
        return value;
    }
}