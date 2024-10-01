package com.zergatul.scripting.runtime;

import com.zergatul.scripting.MethodDescription;

import java.util.Locale;

@SuppressWarnings("unused")
public class Int64Utils {

    @MethodDescription("""
            Formats number with thousands separator. Example: 10000 -> "10,000"
            """)
    public static String toStandardString(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    @MethodDescription("""
            Attempts to parse 64bit integer from string. Returns true if succeeded
            """)
    public static boolean tryParse(String str, Int64Reference reference) {
        try {
            reference.set(Long.parseLong(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
