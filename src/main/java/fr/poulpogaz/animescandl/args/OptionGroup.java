package fr.poulpogaz.animescandl.args;

import fr.poulpogaz.animescandl.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class OptionGroup {

    private final String name;
    private final List<Option> options;

    public OptionGroup(String name) {
        this.name = name;
        this.options = new ArrayList<>();
    }

    public Option getOptionByShortName(String name) {
        return Utils.find(options, (o) -> o.getShortName() != null && o.getShortName().equals(name));
    }

    public Option getOptionByName(String name) {
        return Utils.find(options, (o) -> o.getName() != null && o.getName().equals(name));
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }
}
