package com.zergatul.scripting.tests.highlighting;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.highlighting.SemanticTokenModifier;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.highlighting.helpers.HighlightingHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassTests extends ComparatorTest {

    @Test
    public void visibilityModifiersTest() {
        String code = """
                class Class {
                    public int field;
                    protected constructor() {}
                    private void method() {}
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 7, 6, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 13, 12, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 5, 18, 6)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 12, 25, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 16, 29, 5)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 21, 34, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(3, 5, 40, 9)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(3, 15, 50, 11)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 26, 61, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 27, 62, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 29, 64, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 30, 65, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(4, 5, 71, 7)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(4, 13, 79, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(4, 18, 84, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 24, 90, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 25, 91, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 27, 93, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 28, 94, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 1, 96, 1))),
                highlight(code));
    }

    @Test
    public void binaryOperatorTest() {
        String code = """
                class MyClass {
                    operator [+] MyClass(MyClass left, MyClass right) {
                        return new MyClass();
                    }
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 7, 6, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 15, 14, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 5, 20, 8)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 14, 29, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 15, 30, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 16, 31, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 18, 33, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 25, 40, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 26, 41, 7)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 34, 49, 4)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 38, 53, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 40, 55, 7)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 48, 63, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 53, 68, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 55, 70, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(3, 9, 80, 6)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(3, 16, 87, 3)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(3, 20, 91, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 27, 98, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 28, 99, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(3, 29, 100, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 5, 106, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 1, 108, 1))),
                highlight(code));
    }

    private List<SemanticToken> highlight(String code) {
        return HighlightingHelper.highlight(ApiRoot.class, code);
    }

    public static class ApiRoot {}
}