package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonTreeWriter;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String FFMPEG_PATH = "ffmpeg";
    private static final ASDLLogger LOGGER = Loggers.getLogger(Utils.class);

    public static final String ANSI_BLUE = "36";
    public static final String ANSI_GREEN = "32";
    public static final String ANSI_PURPLE = "35";

    public static String blue(String str) {
        return color(str, ANSI_BLUE);
    }

    public static String green(String str) {
        return color(str, ANSI_GREEN);
    }

    public static String purple(String str) {
        return color(str, ANSI_PURPLE);
    }

    public static String color(String str, String color) {
        return "%s[%sm%s%s[0m".formatted((char) 27, color, str, (char) 27);
    }

    public static String colorStart(String color) {
        return "%s[%sm".formatted((char) 27, color);
    }

    public static String colorEnd() {
        return (char) 27 + "[0m";
    }

    public static String bytesHumanReadable(long totalSize, int precision) {
        if (totalSize < 1024L) {
            return totalSize + " B";
        } else if (totalSize < 1024L * 1024) {
            return ("%." + precision + "f KB").formatted(totalSize / 1024d);
        } else if (totalSize < 1024L * 1024 * 1024) {
            return ("%." + precision + "f MB").formatted(totalSize / (1024d * 1024));
        } else if (totalSize < 1024L * 1024 * 1024 * 1024) {
            return ("%." + precision + "f GB").formatted(totalSize / (1024d * 1024 * 1024));
        } else {
            return ("%." + precision + "f TB").formatted(totalSize / (1024d * 1024 * 1024 * 1024));
        }
    }

    public static String timeHumanReadable(long second, int precision) {
        if (second == Long.MAX_VALUE) {
            return "+infinity s";
        }

        if (second < 60) {
            return second + "s";
        } else if (second < 60 * 60) {
            return ("%." + precision + "f m").formatted(second / 60d);
        } else if (second < 60 * 60 * 24) {
            return ("%." + precision + "f h").formatted(second / (60d * 60));
        } else  {
            return ("%." + precision + "f d").formatted(second / (60d * 60 * 24));
        }
    }

    public static String getRegexGroup(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalStateException();
    }

    public static int getFirstInt(String str) {
        return Integer.parseInt(Utils.getRegexGroup(str, "(\\d+)"));
    }

    public static <T> boolean contains(T[] array, T o) {
        if (o == null) {
            for (T t : array) {
                if (t == null) {
                    return true;
                }
            }
        } else {
            for (T t : array) {
                if (o.equals(t)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <T> T find(Collection<T> list, Predicate<T> predicate) {
        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor)
    {
        Objects.requireNonNull(keyExtractor);
        return (c1, c2) -> {
            U u1 = keyExtractor.apply(c1);
            U u2 = keyExtractor.apply(c2);

            if (u1 == null && u2 == null) {
                return 0;
            } else if (u1 == null) {
                return -1;
            } else if (u2 == null) {
                return 1;
            } else {
                return u1.compareTo(u2);
            }
        };
    }

    public static <K, V> V getOrCreate(Map<K, V> map, K key, Supplier<V> defaultValue) {
        V value = map.get(key);

        if (value == null) {
            value = defaultValue.get();
            map.put(key, value);
        }

        return value;
    }

    public static Optional<Float> parseFloat(String str) {
        try {
            return Optional.of(Float.parseFloat(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static int[] parseRange(String range) {
        if (range == null || range.isEmpty()) {
            throw new IllegalStateException("Range is null");
        }

        String[] ranges = range.split(",");

        int nColon = (int) range.chars().filter((c) -> c == ',').count();

        if (nColon + 1 != ranges.length) { // the number of ranges is the number of colon plus one
            throw new IllegalStateException("Colon problem");
        }

        Set<Integer> values = new LinkedHashSet<>();

        for (String r : ranges) {
            if (r.indexOf('-') != r.lastIndexOf('-')) { // multiple hyphen
                throw new IllegalStateException("Duplicate hyphen");
            }

            String[] limits = r.split("-");

            if (limits.length == 1) {
                int val = Integer.parseInt(limits[0]);

                values.add(val);
            } else if (limits.length == 2) {
                int min = Integer.parseInt(limits[0]);
                int max = Integer.parseInt(limits[1]);

                if (min > max) {
                    throw new IllegalStateException("min can be greater than max");
                }

                for (; min <= max; min++) {
                    values.add(min);
                }
            }
        }

        int[] array = new int[values.size()];

        int i = 0;
        for (Integer v : values) {
            array[i] = v;
            i++;
        }

        return array;
    }

    public static String jsonString(JsonElement element) {
        StringWriter sw = new StringWriter();
        try {
            JsonTreeWriter.write(element, sw);
        } catch (IOException | JsonException e) {
            LOGGER.warnln("Failed to make string of json", e);
        }

        return sw.toString();
    }

    public static String getFirstNonEmptyText(Element element, boolean substring) {
        for (TextNode n : element.textNodes()) {
            String text = n.text();

            if (!text.isBlank() && !text.isEmpty()) {
                if (substring) {
                    return text.substring(1, text.length() - 1);
                } else {
                    return text;
                }
            }
        }

        return "";
    }

    public static <T> T min(T o1, T o2, Comparator<T> comparator) {
        int c = comparator.compare(o1, o2);

        return (c < 0) ? o1 : o2;
    }

    public static <T> T max(T o1, T o2, Comparator<T> comparator) {
        int c = comparator.compare(o1, o2);

        return (c > 0) ? o1 : o2;
    }
}