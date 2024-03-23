package com.zergatul.scripting;

public class SingleLineTextRange extends TextRange {

    private final int line;
    private final int column;
    private final int position;
    private final int length;

    public SingleLineTextRange(int line, int column, int position, int length) {
        this.line = line;
        this.column = column;
        this.position = position;
        this.length = length;
    }

    @Override
    public int getLine1() {
        return this.line;
    }

    @Override
    public int getColumn1() {
        return this.column;
    }

    @Override
    public int getLine2() {
        return this.line;
    }

    @Override
    public int getColumn2() {
        return this.column + this.length;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SingleLineTextRange other) {
            return other.line == line && other.column == column && other.position == position && other.length == length;
        } else {
            return false;
        }
    }
}