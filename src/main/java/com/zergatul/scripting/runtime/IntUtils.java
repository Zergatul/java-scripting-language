package com.zergatul.scripting.runtime;

import com.zergatul.scripting.MethodDescription;

import java.util.Locale;

@SuppressWarnings("unused")
public class IntUtils {

    @MethodDescription("""
            Formats number with thousands separator. Example: 10000 -> "10,000"
            """)
    public static String toStandardString(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    @MethodDescription("""
            Attempts to parse integer from string. Returns true if succeeded
            """)
    public static boolean tryParse(String str, IntReference reference) {
        try {
            reference.set(Integer.parseInt(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}