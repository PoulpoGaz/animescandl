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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String FFMPEG_PATH = "ffmpeg";
    private static final ASDLLogger LOGGER = Loggers.getLogger(Utils.class);

    public static double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static double toMB(long value) {
        BigDecimal _1024 = BigDecimal.valueOf(1024);

        return BigDecimal.valueOf(value)
                .divide(_1024.multiply(_1024), 2, RoundingMode.HALF_EVEN)
                .doubleValue();
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

    public static <T> T findInValue(Map<?, T> list, Predicate<T> predicate) {
        return find(list.values(), predicate);
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
}