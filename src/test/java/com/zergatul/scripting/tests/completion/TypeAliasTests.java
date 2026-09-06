package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SString;
import com.zergatul.scripting.type.SUnknown;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class TypeAliasTests {

    @Test
    public void suggestTypeAliasKeywordTest1() {
        String code =
                "type<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void suggestTypeAliasKeywordTest2() {
        String code =
                "typealias Str = string;\n" +
                "type<cursor>\n" +
                "int x = 1;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new TypeAliasSuggestion(new SAliasType("Str", SString.instance)),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void suggestTypeAliasKeywordTest3() {
        String code =
                "static int a = 1;\n" +
                "typealias Str = string;\n" +
                "type<cursor>\n" +
                "a = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new TypeAliasSuggestion(new SAliasType("Str", SString.instance)),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "a")));
    }

    @Test
    public void singleWordStatementStartAliasedTypeTest() {
        String code =
                "typealias Str = string;\n" +
                "int x = 1;\n" +
                "Str<cursor>\n" +
                "x = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new TypeAliasSuggestion(new SAliasType("Str", SString.instance)),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void singleWordStatementStartDeclaredClassTypeTest() {
        String code =
                "class Class {}\n" +
                "int x = 1;\n" +
                "Class<cursor>\n" +
                "x = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new ClassSuggestion(context, "Class"),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void suggestAliasTypeTest() {
        String code =
                "typealias Str = string;\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new TypeAliasSuggestion(new SAliasType("Str", SString.instance)),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void suggestTypesAfterEquals() {
        String code =
                "typealias MyType = <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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