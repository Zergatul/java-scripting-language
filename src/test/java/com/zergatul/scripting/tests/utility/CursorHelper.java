package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.SingleLineTextRange;
import org.junit.jupiter.api.Assertions;

public class CursorHelper {

    private static final String CURSOR = "<cursor>";

    public static Result parse(String code) {
        int position = code.indexOf(CURSOR);
        if (position < 0) {
            return Assertions.fail();
        }

        int line = -1, column = -1;
        String[] lines = code.lines().toArray(String[]::new);
        for (int i = 0; i < lines.length; i++) {
            int index = lines[i].indexOf(CURSOR);
            if (index >= 0) {
                line = i + 1;
                column = index + 1;
                break;
            }
        }
        if (line == -1) {
            return Assertions.fail();
        }

        return new Result(
                code.replace(CURSOR, ""),
                new SingleLineTextRange(line, column, position, 0));
    }

    public record Result(String code, SingleLineTextRange range) {
        public int line() {
            return range.getLine1();
        }
        public int column() {
            return range.getColumn1();
        }
    }
}