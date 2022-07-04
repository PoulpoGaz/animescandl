package fr.poulpogaz.animescandl.website.filter;

import fr.poulpogaz.json.tree.JsonElement;

public class Text extends Filter<String> {
    
    public Text(String name) {
        super(name, null);
    }

    @Override
    public void setValue(JsonElement value) throws InvalidValueException {
        if (value.isObject() || value.isArray()) {
            throw new InvalidValueException(name + ": Object or array not allowed");
        }

        this.value = value.getAsString();
    }
}