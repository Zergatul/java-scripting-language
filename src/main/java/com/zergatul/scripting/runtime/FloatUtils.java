package com.zergatul.scripting.runtime;

import java.util.Locale;

@SuppressWarnings("unused")
public class FloatUtils {
    public static String toStandardString(double value, int decimals) {
        String format = "%,." + decimals + "f";
        return String.format(Locale.ROOT, format, value);
    }
}