package fr.poulpogaz.animescandl.args;

public class OptionBuilder {

    private String name = null;
    private String shortName = null;
    private String argumentName = null;
    private boolean hasArgument = false;
    private boolean allowMultipleValues = false;
    private boolean optional = true;
    private String description = null;

    public Option build() {
        if (name == null && shortName == null) {
            throw new IllegalStateException("name and shortName can't be null at the same time");
        }

        return new Option(name, shortName, argumentName, hasArgument, allowMultipleValues, optional, description);
    }

    public String name() {
        return name;
    }

    public OptionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public String shortName() {
        return shortName;
    }

    public OptionBuilder shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public String argName() {
        return argumentName;
    }

    public OptionBuilder argName(String argumentName) {
        this.argumentName = argumentName;
        hasArgument = true;
        return this;
    }

    public boolean hasArg() {
        return hasArgument;
    }

    public OptionBuilder hasArg(boolean hasArgument) {
        this.hasArgument = hasArgument;
        return this;
    }

    public boolean multipleValues() {
        return allowMultipleValues;
    }

    public OptionBuilder multipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
        return this;
    }

    public boolean optional() {
        return optional;
    }

    public OptionBuilder optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public String desc() {
        return description;
    }

    public OptionBuilder desc(String description) {
        this.description = description;
        return this;
    }
}
