package fr.poulpogaz.animescandl.website.filter;

import java.util.Collections;
import java.util.List;

public class Select<V> extends Filter<Integer> {

    private final List<V> acceptedValues;

    public Select(String name, List<V> acceptedValues) {
        super(name);
        this.acceptedValues = Collections.unmodifiableList(acceptedValues);
    }

    @Override
    public void setValue(Integer value) {
        if (value >= 0 && value < acceptedValues.size()) {
            this.value = value;
        }
    }

    public List<V> getAcceptedValues() {
        return acceptedValues;
    }
}
