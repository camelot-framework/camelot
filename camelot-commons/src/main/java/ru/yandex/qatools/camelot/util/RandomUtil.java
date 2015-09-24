package ru.yandex.qatools.camelot.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public abstract class RandomUtil {
    RandomUtil() {
    }

    private static final SecureRandom random = new SecureRandom();
    private static Random randomValue = new Random();

    public static int randomInt(int max) {
        return randomValue.nextInt(max);
    }

    public static boolean isProbable(int probability) {
        return randomProbability() < probability;
    }

    public static int randomProbability() {
        return randomValue.nextInt(100);
    }

    public static int randomInt(int from, int to) {
        return from + randomValue.nextInt(to - from);
    }

    public static int randomInt() {
        return randomValue.nextInt();
    }

    public static String randomString() {
        return new BigInteger(130, random).toString(32);
    }
}
