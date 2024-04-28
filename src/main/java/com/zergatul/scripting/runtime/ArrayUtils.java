package com.zergatul.scripting.runtime;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ArrayUtils {

    public static boolean[] concat(boolean[] a1, boolean[] a2) {
        boolean[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static int[] concat(int[] a1, int[] a2) {
        int[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static double[] concat(double[] a1, double[] a2) {
        double[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static char[] concat(char[] a1, char[] a2) {
        char[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static <T> T[] concat(T[] a1, T[] a2) {
        T[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }
}