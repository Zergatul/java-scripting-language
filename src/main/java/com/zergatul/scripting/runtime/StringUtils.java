package com.zergatul.scripting.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("unused")
public class StringUtils {

    @MethodDescription("""
            Returns true if string instance matches specified regex.
            For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
            """)
    public static boolean matches(String input, String regex) {
        return matches(input, regex, 0);
    }

    @MethodDescription("""
            Returns true if string instance matches specified regex.
            Flags is bit mask of:
            - CASE_INSENSITIVE = 0x02
            - MULTILINE = 0x08
            - DOTALL = 0x20
            - UNICODE_CASE = 0x40
            - UNICODE_CHARACTER_CLASS = 0x100
            For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
            """)
    public static boolean matches(String input, String regex, int flags) {
        try {
            return Pattern.compile(regex, flags).matcher(input).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public static String[] getMatches(String input, String regex) {
        return getMatches(input, regex, 0);
    }

    @MethodDescription("""
            If string instance matches specified regex returns array with matches.
            Element at position zero corresponds to entire matched substring.
            Other elements are group matches.
            If string doesn't match regex - returns empty array.
            Flags is bit mask of:
            - CASE_INSENSITIVE = 0x02
            - MULTILINE = 0x08
            - DOTALL = 0x20
            - UNICODE_CASE = 0x40
            - UNICODE_CHARACTER_CLASS = 0x100
            For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
            """)
    public static String[] getMatches(String input, String regex, int flags) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex, flags);
        } catch (PatternSyntaxException e) {
            return new String[0];
        }

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] matches = new String[matcher.groupCount() + 1];
            for (int i = 0; i < matches.length; i++) {
                matches[i] = matcher.group(i);
            }
            return matches;
        } else {
            return new String[0];
        }
    }
}