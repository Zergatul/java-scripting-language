package com.zergatul.scripting.runtime;

import java.util.Locale;

@SuppressWarnings("unused")
public class IntUtils {
    public static String toStandardString(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}