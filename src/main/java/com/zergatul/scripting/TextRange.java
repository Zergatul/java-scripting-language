package com.zergatul.scripting;

public abstract class TextRange {

    public abstract int getLine1();
    public abstract int getColumn1();
    public abstract int getLine2();
    public abstract int getColumn2();
    public abstract int getPosition();
    public abstract int getLength();

    public boolean isEmpty() {
        return getLength() == 0;
    }

    public boolean contains(int line, int column) {
        if (line < getLine1()) {
            return false;
        }
        if (line > getLine2()) {
            return false;
        }
        if (line == getLine1() && column < getColumn1()) {
            return false;
        }
        if (line == getLine2() && column >= getColumn2()) {
            return false;
        }
        return true;
    }

    public boolean endsWith(int line, int column) {
        return line == getLine2() && column == getColumn2();
    }

    public boolean containsOrEnds(int line, int column) {
        if (line < getLine1()) {
            return false;
        }
        if (line > getLine2()) {
            return false;
        }
        if (line == getLine1() && column < getColumn1()) {
            return false;
        }
        if (line == getLine2() && column > getColumn2()) {
            return false;
        }
        return true;
    }

    public boolean isBefore(int line, int column) {
        if (getLine2() < line) {
            return true;
        }
        if (getLine2() > line) {
            return false;
        }
        return getColumn1() < column;
    }

    public boolean isAfter(int line, int column) {
        if (getLine1() < line) {
            return false;
        }
        if (getLine1() > line) {
            return true;
        }
        return getColumn1() > column;
    }

    public TextRange getEnd() {
        return new SingleLineTextRange(getLine2(), getColumn2(), getPosition() + getLength(), 0);
    }

    public static TextRange inner(Locatable locatable1, Locatable locatable2) {
        return inner(locatable1.getRange(), locatable2.getRange());
    }

    public static TextRange inner(TextRange range1, TextRange range2) {
        TextRange begin = range1.subRange(range1.getLength());
        if (begin.getLine1() == range2.getLine1()) {
            return new SingleLineTextRange(
                    begin.getLine1(),
                    begin.getColumn1(),
                    begin.getPosition(),
                    range2.getPosition() - begin.getPosition());
        } else {
            return new MultiLineTextRange(
                    begin.getLine1(),
                    begin.getColumn1(),
                    range2.getLine1(),
                    range2.getColumn1(),
                    begin.getPosition(),
                    range2.getPosition() - begin.getPosition());
        }
    }

    public static boolean isBetween(int line, int column, TextRange range1, TextRange range2) {
        if (line < range1.getLine1()) {
            return false;
        }
        if (line == range1.getLine1() && column < range1.getColumn1()) {
            return false;
        }
        if (line > range2.getLine2()) {
            return false;
        }
        if (line == range2.getLine2() && column > range2.getColumn2()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextRange other) {
            return  other.getLine1() == getLine1() &&
                    other.getColumn1() == getColumn1() &&
                    other.getLine2() == getLine2() &&
                    other.getColumn2() == getColumn2() &&
                    other.getPosition() == getPosition() &&
                    other.getLength() == getLength();
        } else {
            return false;
        }
    }

    public String extract(String str) {
        return str.substring(getPosition(), getPosition() + getLength());
    }

    public abstract TextRange subRange(int skip);

    public static TextRange combine(TextRange range1, TextRange range2) {
        if (range1.getLine1() == range2.getLine2()) {
            return new SingleLineTextRange(
                    range1.getLine1(),
                    range1.getColumn1(),
                    range1.getPosition(),
                    range2.getPosition() + range2.getLength() - range1.getPosition());
        } else {
            return new MultiLineTextRange(
                    range1.getLine1(),
                    range1.getColumn1(),
                    range2.getLine2(),
                    range2.getColumn2(),
                    range1.getPosition(),
                    range2.getPosition() + range2.getLength() - range1.getPosition());
        }
    }

    public static TextRange combine(Locatable locatable1, Locatable locatable2) {
        return combine(locatable1.getRange(), locatable2.getRange());
    }

    public static TextRange combineFromEnd(Locatable locatable1, Locatable locatable2) {
        return combine(locatable1.getRange().getEnd(), locatable2.getRange());
    }
}