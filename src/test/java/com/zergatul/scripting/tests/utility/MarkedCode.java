package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MarkedCode {

    private final String code;
    private final Map<String, TextRange> ranges;

    private MarkedCode(String code, Map<String, TextRange> ranges) {
        this.code = code;
        this.ranges = ranges;
    }

    public static MarkedCode from(String text) {
        MarkedParser parser = new MarkedParser(text);
        parser.process();
        return new MarkedCode(parser.code.toString(), parser.ranges);
    }

    public String getCode() {
        return code;
    }

    public TextRange getRange(String key) {
        return Objects.requireNonNull(ranges.get(key));
    }

    private static class MarkedParser {

        private final String text;
        private final StringBuilder code;
        private final Map<String, TextRange> ranges;
        private final Map<String, Position> pendingRanges;
        private final List<BracketPair> bracketPairs;
        private int inputPosition;
        private int outputPosition;
        private int line;
        private int column;

        public MarkedParser(String text) {
            this.text = text;
            this.code = new StringBuilder();
            this.ranges = new HashMap<>();
            this.pendingRanges = new HashMap<>();
            this.bracketPairs = Lists.of(
                    new BracketPair('⟪', '⟫'),
                    new BracketPair('⟦', '⟧'),
                    new BracketPair('❬', '❭'),
                    new BracketPair('❰', '❱'),
                    new BracketPair('⟨', '⟩'),
                    new BracketPair('⁅', '⁆'),
                    new BracketPair('⟬', '⟭'),
                    new BracketPair('⸨', '⸩'));
            this.line = 1;
            this.column = 1;
            this.inputPosition = 0;
            this.outputPosition = 0;
        }

        public void process() {
            while (inputPosition < text.length()) {
                BracketPair openingPair = getOpeningPair();
                BracketPair closingPair = getClosingPair();
                if (openingPair != null) {
                    startRange(openingPair);
                    inputPosition++;
                } else if (closingPair != null) {
                    endRange(closingPair);
                    inputPosition++;
                } else {
                    appendCurrent();
                }
            }

            if (!pendingRanges.isEmpty()) {
                throw new IllegalArgumentException("Unclosed marker.");
            }
        }

        private void appendCurrent() {
            char ch = text.charAt(inputPosition);
            code.append(ch);
            inputPosition++;
            outputPosition++;

            if (ch == '\r') {
                if (inputPosition < text.length() && text.charAt(inputPosition) == '\n') {
                    code.append('\n');
                    inputPosition++;
                    outputPosition++;
                    newLine();
                } else {
                    newLine();
                }
                return;
            }

            if (ch == '\n') {
                newLine();
            } else {
                column++;
            }
        }

        private void startRange(BracketPair pair) {
            String key = pair.key();
            if (ranges.containsKey(key) || pendingRanges.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicate marker '%s'.", key));
            }
            pendingRanges.put(key, new Position(line, column, outputPosition));
        }

        private void endRange(BracketPair pair) {
            String key = pair.key();
            Position start = pendingRanges.remove(key);
            if (start == null) {
                throw new IllegalArgumentException(String.format("Unexpected closing marker '%s'.", key));
            }

            TextRange range = start.line == line ?
                    new SingleLineTextRange(start.line, start.column, start.position, outputPosition - start.position) :
                    new MultiLineTextRange(start.line, start.column, line, column, start.position, outputPosition - start.position);
            ranges.put(key, range);
        }

        private void newLine() {
            line++;
            column = 1;
        }

        private BracketPair getOpeningPair() {
            char ch = text.charAt(inputPosition);
            return bracketPairs.stream()
                    .filter(pair -> pair.open == ch)
                    .findFirst()
                    .orElse(null);
        }

        private BracketPair getClosingPair() {
            char ch = text.charAt(inputPosition);
            return bracketPairs.stream()
                    .filter(pair -> pair.close == ch)
                    .findFirst()
                    .orElse(null);
        }
    }

    private static final class Position {

        private final int line;
        private final int column;
        private final int position;

        private Position(int line, int column, int position) {
            this.line = line;
            this.column = column;
            this.position = position;
        }

        public int line() {
            return line;
        }

        public int column() {
            return column;
        }

        public int position() {
            return position;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Position that = (Position) obj;
            return  this.line == that.line &&
                    this.column == that.column &&
                    this.position == that.position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, column, position);
        }

        @Override
        public String toString() {
            return  "Position[" +
                    "line=" + line + ", " +
                    "column=" + column + ", " +
                    "position=" + position + ']';
        }
    }

    private static final class BracketPair {

        private final char open;
        private final char close;

        private BracketPair(char open, char close) {
            this.open = open;
            this.close = close;
        }

        private String key() {
            return String.valueOf(open) + close;
        }

        public char open() {
            return open;
        }

        public char close() {
            return close;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            BracketPair that = (BracketPair) obj;
            return  this.open == that.open &&
                    this.close == that.close;
        }

        @Override
        public int hashCode() {
            return Objects.hash(open, close);
        }

        @Override
        public String toString() {
            return  "BracketPair[" +
                    "open=" + open + ", " +
                    "close=" + close + ']';
        }
    }
}