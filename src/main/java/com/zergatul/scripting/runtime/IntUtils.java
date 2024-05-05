package com.zergatul.scripting.runtime;

import java.util.Locale;

@SuppressWarnings("unused")
public class IntUtils {

    public static String toStandardString(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    public static boolean tryParse(String str, IntReference reference) {
        try {
            reference.set(Integer.parseInt(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}