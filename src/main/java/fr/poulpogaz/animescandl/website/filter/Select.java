package fr.poulpogaz.animescandl.website.filter;

import fr.poulpogaz.json.tree.JsonElement;

import java.util.Collections;
import java.util.List;

public class Select<V> extends Filter<Integer> {

    private final List<V> acceptedValues;

    public Select(String name, List<V> acceptedValues) {
        super(name);
        this.acceptedValues = Collections.unmodifiableList(acceptedValues);
    }

    @Override
    public void setValue(JsonElement value) throws InvalidValueException {
        int v = -1;

        if (value.isBoolean() || value.isNumber()) {
            v = value.getAsInt();

        } else if (value.isString()) {

            for (int i = 0; i < acceptedValues.size(); i++) {
                if (acceptedValues.get(i).toString()
                        .equals(value.getAsString())) {
                    v = i;
                }
            }

            if (v == -1) {
                v = value.optionalInt().orElse(-1);
            }

        } else {
            throw new InvalidValueException(name + ": Expecting boolean or int or string");
        }

        if (v < 0 || v >= acceptedValues.size()) {
            throw new InvalidValueException(name + ": Invalid value: " + value.getAsString());
        }

        this.value = v;
    }

    @Override
    public void setValue(Integer value) {
        if (value == null) {
            this.value = null;
        } else if (value >= 0 && value < acceptedValues.size()) {
            this.value = value;
        }
    }

    public List<V> getAcceptedValues() {
        return acceptedValues;
    }
}
