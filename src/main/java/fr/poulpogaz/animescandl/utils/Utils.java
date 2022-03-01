package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.website.Mangadex;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeWriter;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String FFMPEG_PATH = "ffmpeg";
    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    public static boolean WRITE = false;
    public static boolean VERBOSE = false;

    public static Logger getLogger(Class<?> clasz, String url) {
        return LogManager.getLogger("%s-%s".formatted(clasz.getSimpleName(), url));
    }

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

    public static boolean isValidURL(String url) {
        return UrlValidator.getInstance().isValid(url);
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
            LOGGER.warn("Failed to make string of json", e);
        }

        return sw.toString();
    }

    public static int chapterComparator(Chapter a, Chapter b) {
        Pattern pattern = Pattern.compile("(\\d+)");

        Matcher matcherA = pattern.matcher(a.chapter());
        Matcher matcherB = pattern.matcher(b.chapter());

        if (matcherA.find() && matcherB.find()) {
            int min = Math.min(matcherA.groupCount(), matcherB.groupCount());
            for (int i = 1; i <= min; i++) {
                int intA = Integer.parseInt(matcherA.group(i));
                int intB = Integer.parseInt(matcherB.group(i));

                if (intA < intB) {
                    return -1;
                } else if (intA > intB) {
                    return 1;
                }
            }

            return Integer.compare(matcherA.groupCount(), matcherB.groupCount());
        } else {
            return 0;
        }
    }
}