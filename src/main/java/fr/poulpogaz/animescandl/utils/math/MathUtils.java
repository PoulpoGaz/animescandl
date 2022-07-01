package fr.poulpogaz.animescandl.utils.math;

public class MathUtils {

    //public static Set parseRange(String range) {
    //    if (range == null || range.isEmpty()) {
    //        throw new IllegalStateException("Range is null");
    //    }

    //    String[] ranges = range.split(",");

    //    int nColon = (int) range.chars().filter((c) -> c == ',').count();

    //    if (nColon + 1 != ranges.length) { // the number of ranges is the number of colon plus one
    //        throw new IllegalStateException("Colon problem");
    //    }

    //    java.util.Set<Integer> values = new LinkedHashSet<>();

    //    for (String r : ranges) {
    //        if (r.indexOf('-') != r.lastIndexOf('-')) { // multiple hyphen
    //            throw new IllegalStateException("Duplicate hyphen");
    //        }

    //        String[] limits = r.split("-");

    //        if (limits.length == 1) {
    //            int val = Integer.parseInt(limits[0]);

    //            values.add(val);
    //        } else if (limits.length == 2) {
    //            int min = Integer.parseInt(limits[0]);
    //            int max = Integer.parseInt(limits[1]);

    //            if (min > max) {
    //                throw new IllegalStateException("min can be greater than max");
    //            }

    //            for (; min <= max; min++) {
    //                values.add(min);
    //            }
    //        }
    //    }

    //    int[] array = new int[values.size()];

    //    int i = 0;
    //    for (Integer v : values) {
    //        array[i] = v;
    //        i++;
    //    }

    //    return array;
    //}
}
