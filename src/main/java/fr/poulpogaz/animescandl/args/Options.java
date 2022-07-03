package fr.poulpogaz.animescandl.args;

import java.util.LinkedHashMap;

public class Options {

    private final LinkedHashMap<String, OptionGroup> options;

    public Options() {
        options = new LinkedHashMap<>();
    }

    public String parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            Option option = null;

            if (arg.startsWith("--")) {
                option = findOptionByName(arg.substring(2));
            } else if (arg.startsWith("-")) {
                option = findOptionByShortName(arg.substring(1));
            }

            if (option == null) {
                return "Unrecognized parameter: " + arg;
            }

            if (option.isPresent() && !option.allowMultipleValues()) {
                return "Duplicate parameter: " + option.getName();
            }

            if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                if (option.hasArgument()) {
                    i++;
                    option.addArguments(args[i]);
                } else {
                    return "Option %s doesn't require a parameter".formatted(arg);
                }
            } else if (option.hasArgument()) {
                return "Option %s require a parameter".formatted(arg);
            }

            option.markPresent();
        }

        for (OptionGroup group : options.values()) {
            for (Option option : group.getOptions()) {
                if (!option.isOptional() && !option.isPresent()) {
                    return option.getName() + " is required";
                }
            }
        }

        return null;
    }

    protected Option findOptionByName(String name) {
        for (OptionGroup group : options.values()) {
            Option opt = group.getOptionByName(name);

            if (opt != null) {
                return opt;
            }
        }

        return null;
    }

    protected Option findOptionByShortName(String name) {
        for (OptionGroup group : options.values()) {
            Option opt = group.getOptionByShortName(name);

            if (opt != null) {
                return opt;
            }
        }

        return null;
    }

    /**
     * The option will be added to the unnamed group
     * @param option the option to add
     */
    public Options addOption(Option option) {
        addOption(null, option);
        return this;
    }

    public Options addOptionGroup(OptionGroup group) {
        options.put(group.getName(), group);
        return this;
    }

    public Options addOption(String group, Option option) {
        OptionGroup optGroup = options.get(group);

        if (optGroup == null) {
            optGroup = new OptionGroup(group);
            optGroup.addOption(option);
            options.put(group, optGroup);
        } else {
            optGroup.addOption(option);
        }
        return this;
    }

    public OptionGroup getGroup(String name) {
        return options.get(name);
    }

    public LinkedHashMap<String, OptionGroup> getOptions() {
        return options;
    }
}
