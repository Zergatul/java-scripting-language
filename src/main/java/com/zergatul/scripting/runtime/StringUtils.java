package com.zergatul.scripting.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("unused")
public class StringUtils {

    public static boolean matches(String input, String regex) {
        try {
            return Pattern.compile(regex).matcher(input).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public static String[] getMatches(String input, String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
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