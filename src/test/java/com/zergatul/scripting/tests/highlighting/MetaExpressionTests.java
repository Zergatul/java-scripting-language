package com.zergatul.scripting.tests.highlighting;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.highlighting.SemanticTokenModifier;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.highlighting.helpers.HighlightingHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MetaExpressionTests extends ComparatorTest {

    @Test
    public void castExpressionTest() {
        String code = """
                let x = #cast(123, string);
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 7, 6, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 9, 8, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.NUMBER, new SingleLineTextRange(1, 15, 14, 3)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 18, 17, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 20, 19, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 26, 25, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 27, 26, 1))),
                highlight(code));
    }

    private List<SemanticToken> highlight(String code) {
        return HighlightingHelper.highlight(ApiRoot.class, code);
    }

    public static class ApiRoot {}
}