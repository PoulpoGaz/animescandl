package fr.poulpogaz.animescandl.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Option {

    private final String name;
    private final String shortName;
    private final String argumentName;
    private final boolean hasArgument;
    private final boolean allowMultipleValues;
    private final boolean optional;
    private final String description;

    private final List<String> arguments = new ArrayList<>();
    private boolean present;

    public Option(String name, String shortName, String argumentName, boolean hasArgument, boolean allowMultipleValues, boolean optional, String description) {
        this.name = name;
        this.shortName = shortName;
        this.argumentName = argumentName;
        this.hasArgument = hasArgument;
        this.allowMultipleValues = allowMultipleValues;
        this.optional = optional;
        this.description = description;
    }

    public void addArguments(String args) {
        arguments.add(args);
    }

    public void markPresent() {
        present = true;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public boolean hasArgument() {
        return hasArgument;
    }

    public boolean allowMultipleValues() {
        return allowMultipleValues;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getArgumentsList() {
        return arguments;
    }

    public Optional<String> getArgument(int index) {
        if (index < 0 || index >= arguments.size()) {
            return Optional.empty();
        } else {
            return Optional.of(arguments.get(index));
        }
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isNotPresent() {
        return !present;
    }

    @Override
    public String toString() {
        return "Option{" +
                "name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", argumentName='" + argumentName + '\'' +
                ", hasArgument=" + hasArgument +
                ", allowMultipleValues=" + allowMultipleValues +
                ", optional=" + optional +
                ", description='" + description + '\'' +
                ", arguments=" + arguments +
                ", present=" + present +
                '}';
    }
}