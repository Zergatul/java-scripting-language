package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class UnitStructureTests {

    @Test
    public void emptyFileTest() {
        assertSuggestions("", 1, 1, context -> List.of(
                new StaticKeywordSuggestion(),
                new VoidKeywordSuggestion(),
                new LetKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void beforeUnitMembersTest() {
        assertSuggestions("""
                
                static int x = 1;
                """, 1, 1, context -> List.of(
                new StaticKeywordSuggestion(),
                new VoidKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance)));
    }

    @Test
    public void afterUnitMembersTest1() {
        assertSuggestions("""
                static int x = 1;
                
                """, 2, 1, context -> List.of(
                new StaticKeywordSuggestion(),
                new VoidKeywordSuggestion(),
                new LetKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterUnitMembersTest2() {
        assertSuggestions("""
                static int x = 1;
                
                int y = 3;
                """, 2, 1, context -> List.of(
                new StaticKeywordSuggestion(),
                new VoidKeywordSuggestion(),
                new LetKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterStatementsTest() {
        assertSuggestions("""
                int x = 3;
                
                """, 2, 1, context -> List.of(
                new LetKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x")));
    }

    private void assertSuggestions(String code, int line, int column, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, line, column, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}