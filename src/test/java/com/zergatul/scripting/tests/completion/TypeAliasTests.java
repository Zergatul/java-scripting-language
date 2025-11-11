package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SString;
import com.zergatul.scripting.type.SUnknown;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class TypeAliasTests {

    @Test
    public void suggestAliasTypeTest() {
        assertSuggestions("""
                typealias Str = string;
                <cursor>
                """,
                context -> Lists.of(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new TypeAliasSuggestion(new SAliasType("Str", SString.instance)),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void suggestTypesAfterEquals() {
        assertSuggestions("""
                typealias MyType = <cursor>
                """,
                context -> Lists.of(
                        types,
                        new TypeAliasSuggestion(new SAliasType("MyType", SUnknown.instance))));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}