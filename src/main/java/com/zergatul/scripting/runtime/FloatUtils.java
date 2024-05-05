package com.zergatul.scripting.runtime;

import java.util.Locale;

@SuppressWarnings("unused")
public class FloatUtils {

    public static String toStandardString(double value, int decimals) {
        String format = "%,." + decimals + "f";
        return String.format(Locale.ROOT, format, value);
    }

    public static boolean tryParse(String str, FloatReference reference) {
        try {
            reference.set(Double.parseDouble(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}