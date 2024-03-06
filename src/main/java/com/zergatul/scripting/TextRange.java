package com.zergatul.scripting;

public abstract class TextRange {

    public abstract int getLine1();
    public abstract int getColumn1();
    public abstract int getLine2();
    public abstract int getColumn2();
    public abstract int getPosition();
    public abstract int getLength();

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

    public static TextRange combine(TextRange range1, TextRange range2) {
        if (range1.getLine1() == range2.getLine2()) {
            return new SingleLineTextRange(
                    range1.getLine1(),
                    range1.getColumn1(),
                    range1.getPosition(),
                    range2.getPosition() + range2.getLength() - range1.getPosition());
        } else {
            return new SingleLineTextRange(
                    range1.getLine1(),
                    range1.getColumn1(),
                    range1.getPosition(),
                    range2.getPosition() + range2.getLength() - range1.getPosition());
        }
    }

    public static TextRange combine(Locatable locatable1, Locatable locatable2) {
        return combine(locatable1.getRange(), locatable2.getRange());
    }
}