package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.ParseException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Configuration implements Iterable<Task> {

    private final List<Task> tasks;

    public Configuration() {
        tasks = new ArrayList<>();
    }

    public void load(Path path) throws IOException, JsonException, ParseException {
        tasks.clear();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            JsonElement root = JsonTreeReader.read(br);

            if (!root.isArray()) {
                throw new ParseException("JSON must be an array");
            }

            JsonArray array = (JsonArray) root;

            int number = 1;
            for (JsonElement e : array) {
                if (e.isObject()) {
                    tasks.add(new ComplexTask((JsonObject) e, number));
                } else if (e.isString()) {
                    tasks.add(new SimpleTask(e.getAsString(), number));
                } else {
                    throw new ParseException("A target must be a string or an object");
                }

                number++;
            }
        }
    }

    @Override
    public Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
