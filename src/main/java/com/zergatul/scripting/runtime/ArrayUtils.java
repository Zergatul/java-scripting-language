package com.zergatul.scripting.runtime;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ArrayUtils {

    public static boolean[] concat(boolean[] a1, boolean element) {
        boolean[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static boolean[] concat(boolean[] a1, boolean[] a2) {
        boolean[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static byte[] concat(byte[] a1, byte element) {
        byte[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static byte[] concat(byte[] a1, byte[] a2) {
        byte[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static short[] concat(short[] a1, short element) {
        short[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static short[] concat(short[] a1, short[] a2) {
        short[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static int[] concat(int[] a1, int element) {
        int[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static int[] concat(int[] a1, int[] a2) {
        int[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static long[] concat(long[] a1, long element) {
        long[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static long[] concat(long[] a1, long[] a2) {
        long[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static float[] concat(float[] a1, float element) {
        float[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static float[] concat(float[] a1, float[] a2) {
        float[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static double[] concat(double[] a1, double element) {
        double[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static double[] concat(double[] a1, double[] a2) {
        double[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static char[] concat(char[] a1, char element) {
        char[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static char[] concat(char[] a1, char[] a2) {
        char[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    public static <T> T[] concat(T[] a1, T element) {
        T[] result = Arrays.copyOf(a1, a1.length + 1);
        result[a1.length] = element;
        return result;
    }

    public static <T> T[] concat(T[] a1, T[] a2) {
        T[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }
}