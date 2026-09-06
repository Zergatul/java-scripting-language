package com.zergatul.scripting.lexer;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class Line {

    private final int beginPosition;
    private final int length;
    private final int endPosition;

    public Line(int beginPosition, int length, int endPosition) {
        this.beginPosition = beginPosition;
        this.length = length;
        this.endPosition = endPosition;
    }

    public int beginPosition() {
        return beginPosition;
    }

    public int length() {
        return length;
    }

    public int endPosition() {
        return endPosition;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Line that = (Line) obj;
        return  this.beginPosition == that.beginPosition &&
                this.length == that.length &&
                this.endPosition == that.endPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginPosition, length, endPosition);
    }

    @Override
    public String toString() {
        return  "Line[" +
                "beginPosition=" + beginPosition + ", " +
                "length=" + length + ", " +
                "endPosition=" + endPosition + ']';
    }
}