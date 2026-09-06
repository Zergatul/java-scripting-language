package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.SingleLineTextRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

public class CursorHelper {

    private static final String CURSOR = "<cursor>";

    public static Result parse(String code) {
        int position = code.indexOf(CURSOR);
        if (position < 0) {
            return Assertions.fail();
        }

        int line = -1, column = -1;
        String[] lines = code.split("\\r\\n|\\r|\\n");
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

    public static final class Result {

        private final String code;
        private final SingleLineTextRange range;

        public Result(String code, SingleLineTextRange range) {
            this.code = code;
            this.range = range;
        }

        public int line() {
            return range.getLine1();
        }

        public int column() {
            return range.getColumn1();
        }

        public String code() {
            return code;
        }

        public SingleLineTextRange range() {
            return range;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Result that = (Result) obj;
            return  Objects.equals(this.code, that.code) &&
                    Objects.equals(this.range, that.range);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, range);
        }

        @Override
        public String toString() {
            return "Result[" +
                    "code=" + code + ", " +
                    "range=" + range + ']';
        }
    }
}