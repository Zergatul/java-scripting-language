package com.zergatul.scripting;

public class MultiLineTextRange extends TextRange {

    private final int line1;
    private final int column1;
    private final int line2;
    private final int column2;
    private final int position;
    private final int length;

    public MultiLineTextRange(int line1, int column1, int line2, int column2, int position, int length) {
        this.line1 = line1;
        this.column1 = column1;
        this.line2 = line2;
        this.column2 = column2;
        this.position = position;
        this.length = length;
    }

    @Override
    public int getLine1() {
        return this.line1;
    }

    @Override
    public int getColumn1() {
        return this.column1;
    }

    @Override
    public int getLine2() {
        return this.line2;
    }

    @Override
    public int getColumn2() {
        return this.column2;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public int getLength() {
        return this.length;
    }
}