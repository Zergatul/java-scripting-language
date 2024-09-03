package com.zergatul.scripting.tests;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextRangeTests {

    @Test
    public void containsTest() {
        TextRange range = new SingleLineTextRange(1, 1, 0, 10);
        Assertions.assertTrue(range.contains(1, 10));
        Assertions.assertFalse(range.contains(1, 11));
    }

    @Test
    public void endsWithTest() {
        TextRange range = new SingleLineTextRange(1, 1, 0, 10);
        Assertions.assertFalse(range.endsWith(1, 10));
        Assertions.assertTrue(range.endsWith(1, 11));
        Assertions.assertFalse(range.endsWith(1, 12));
    }

    @Test
    public void containsOrEndsTest() {
        TextRange range = new SingleLineTextRange(1, 1, 0, 10);
        Assertions.assertTrue(range.containsOrEnds(1, 10));
        Assertions.assertTrue(range.containsOrEnds(1, 11));
        Assertions.assertFalse(range.containsOrEnds(1, 12));
    }

    @Test
    public void isBeforeTest() {
        TextRange range = new SingleLineTextRange(1, 10, 0, 1);
        Assertions.assertFalse(range.isBefore(1, 9));
        Assertions.assertFalse(range.isBefore(1, 10));
        Assertions.assertTrue(range.isBefore(1, 11));
        Assertions.assertTrue(range.isBefore(1, 12));
    }

    @Test
    public void isAfterTest() {
        TextRange range = new SingleLineTextRange(1, 10, 0, 1);
        Assertions.assertTrue(range.isAfter(1, 9));
        Assertions.assertFalse(range.isAfter(1, 10));
        Assertions.assertFalse(range.isAfter(1, 11));
        Assertions.assertFalse(range.isAfter(1, 12));
    }

    @Test
    public void isBetweenTest() {
        Assertions.assertTrue(TextRange.isBetween(
                1, 2,
                new SingleLineTextRange(1, 1, 0, 1),
                new SingleLineTextRange(1,2, 1, 1)));
    }
}