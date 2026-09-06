package com.zergatul.scripting.runtime;

import com.zergatul.scripting.MethodDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("unused")
public class StringUtils {

    @MethodDescription(
            "Returns true if string instance matches specified regex.\n" +
            "For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
    public static boolean matches(String input, String regex) {
        return matches(input, regex, 0);
    }

    @MethodDescription(
            "Returns true if string instance matches specified regex.\n" +
            "Flags is bit mask of:\n" +
            "- CASE_INSENSITIVE = 0x02\n" +
            "- MULTILINE = 0x08\n" +
            "- DOTALL = 0x20\n" +
            "- UNICODE_CASE = 0x40\n" +
            "- UNICODE_CHARACTER_CLASS = 0x100\n" +
            "For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
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

    @MethodDescription(
            "If string instance matches specified regex returns array with matches.\n" +
            "Element at position zero corresponds to entire matched substring.\n" +
            "Other elements are group matches.\n" +
            "If string doesn't match regex - returns empty array.\n" +
            "Flags is bit mask of:\n" +
            "- CASE_INSENSITIVE = 0x02\n" +
            "- MULTILINE = 0x08\n" +
            "- DOTALL = 0x20\n" +
            "- UNICODE_CASE = 0x40\n" +
            "- UNICODE_CHARACTER_CLASS = 0x100\n" +
            "For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
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

    public static String[] split(String input, char separator) {
        return split(input, String.valueOf(separator));
    }

    public static String[] split(String input, String separator) {
        if (separator.isEmpty()) {
            String[] result = new String[input.length()];
            for (int i = 0; i < input.length(); i++) {
                result[i] = String.valueOf(input.charAt(i));
            }
            return result;
        }

        List<String> parts = new ArrayList<>();
        int sepLen = separator.length();
        int start = 0;

        while (true) {
            int index = input.indexOf(separator, start);
            if (index < 0) {
                parts.add(input.substring(start));
                break;
            }

            parts.add(input.substring(start, index));
            start = index + sepLen;
        }

        String[] result = new String[parts.size()];
        return parts.toArray(result);
    }

    public static String[] regexSplit(String input, String regex) {
        return input.split(regex);
    }
}