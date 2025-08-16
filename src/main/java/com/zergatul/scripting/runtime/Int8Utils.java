package com.zergatul.scripting.runtime;

import com.zergatul.scripting.MethodDescription;

import java.util.Locale;

@SuppressWarnings("unused")
public class Int8Utils {

    @MethodDescription("""
            Attempts to parse integer from string. Returns true if succeeded
            """)
    public static boolean tryParse(String str, Int8Reference reference) {
        try {
            reference.set(Byte.parseByte(str));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}