package org.edx.mobile.util;

import java.util.HashSet;
import java.util.Set;

public class JavaUtil {
    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static Set<Long> primitiveLongToSet(long[] longs) {
        HashSet<Long> set = new HashSet<Long>(longs.length);
        for (long l : longs) {
            set.add(l);
        }
        return set;
    }

}
