package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.ParseException;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.animescandl.website.filter.InvalidValueException;
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

    private static final ASDLLogger LOGGER = Loggers.getLogger(Configuration.class);

    private final List<Task> tasks;

    // old tasks and SearchResultTask
    private List<Task> afterSearch;
    private boolean hasSearchTask;

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
                Task task;

                if (e.isObject()) {
                    task = new ComplexTask((JsonObject) e, number);
                } else if (e.isString()) {
                    task = new SimpleTask(e.getAsString(), number);
                } else {
                    throw new ParseException("A target must be a string or an object");
                }

                if (!task.isValid()) {
                    LOGGER.warnln("Invalid task: {}", task.error());
                    continue;
                }

                if (task.isSearchTask()) {
                    hasSearchTask = true;
                }

                tasks.add(task);

                number++;
            }
        }
    }

    public void search() {
        if (!hasSearchTask) {
            return;
        }

        int maxLength = getMaxLength();

        LOGGER.infoln(Utils.centered("Searching", maxLength, '*', ' '));

        afterSearch = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isSearchTask()) {
                printTask(task, maxLength);

                SearchResultTask result = null;
                try {
                    result = task.search();
                } catch (JsonException | IOException | WebsiteException | InterruptedException e) {
                    LOGGER.throwing(e);
                } catch (InvalidValueException e) {
                    LOGGER.errorln(e.getMessage());
                }

                if (result != null) {
                    afterSearch.add(task);
                }

            } else {
                afterSearch.add(task);
            }
        }
    }

    public void download() {
        int maxLength = getMaxLength();

        LOGGER.infoln(Utils.centered("Downloading", maxLength, '*', ' '));

        for (Task task : tasks) {
            printTask(task, maxLength);

            task.download();
        }
    }

    private void printTask(Task task, int maxLength) {
        if (task.name() == null) {
            LOGGER.infoln(Utils.centered("Task n°" + task.number, maxLength, '*', ' '));
        } else {
            LOGGER.infoln(Utils.centered(task.name(), maxLength, '*', ' '));
        }
    }

    private int getMaxLength() {
        int maxLength = 50;

        for (Task task : tasks) {
            if (task.name() == null) {
                maxLength = Math.max(maxLength, 7 + Utils.stringLength(maxLength));
            } else {
                maxLength = Math.max(maxLength, task.name().length());
            }
        }

        return maxLength + 4;
    }

    @Override
    public Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
