package com.smartlaundry.util;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {
    private RandomUtils() { }

    public static int randomInclusive(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min cannot be greater than max");
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean chance(double probability) {
        if (probability <= 0.0) return false;
        if (probability >= 1.0) return true;
        return ThreadLocalRandom.current().nextDouble() < probability;
    }
}
