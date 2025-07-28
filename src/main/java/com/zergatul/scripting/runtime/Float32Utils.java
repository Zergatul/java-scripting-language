package com.zergatul.scripting.runtime;

import java.util.Locale;

@SuppressWarnings("unused")
public class Float32Utils {

    public static String toStandardString(float value, int decimals) {
        String format = "%,." + decimals + "f";
        return String.format(Locale.ROOT, format, value);
    }

    public static boolean tryParse(String str, Float32Reference reference) {
        try {
            reference.set(Float.parseFloat(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}