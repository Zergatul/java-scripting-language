package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.MethodSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.PropertySuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.type.FieldPropertyReference;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;

public class JavaTypeTests {

    @Test
    public void packageTest() {
        assertSuggestions("""
                Java<<cursor>
                """,
                context -> List.of());
    }

    @Test
    public void publicInstanceMembersTest() {
        List<Suggestion> suggestions = Arrays.stream(Hashtable.class.getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(m -> (Suggestion) MethodSuggestion.getInstance(SType.fromJavaType(Hashtable.class), m))
                .toList();
        assertSuggestions("""
                let table = new Java<java.util.Hashtable>();
                table.<cursor>
                """,
                context -> suggestions);
    }

    @Test
    public void publicStaticMembersTest() {
        List<Suggestion> suggestions = new ArrayList<>();
        for (Field field : System.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            suggestions.add(new PropertySuggestion(new FieldPropertyReference(field)));
        }
        for (Method method : System.class.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            suggestions.add(new MethodSuggestion(new NativeMethodReference(method)));
        }
        assertSuggestions("""
                Java<java.lang.System>.<cursor>
                """,
                context -> suggestions);
    }

    @Test
    public void privateInstanceMembersTest() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(PropertySuggestion.getInstance(MyClass.class, "field"));
        suggestions.add(MethodSuggestion.getInstance(MyClass.class, "getField"));
        suggestions.add(MethodSuggestion.getInstance(MyClass.class, "setField"));
        assertSuggestions("""
                let instance = new Java<com.zergatul.scripting.tests.completion.JavaTypeTests$MyClass>();
                instance.#<cursor>
                """,
                context -> suggestions);
    }

    @Test
    public void privateStaticMembersTest() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(PropertySuggestion.getStatic(MyClass.class, "staticField"));
        suggestions.add(MethodSuggestion.getStatic(MyClass.class, "staticMethod"));
        assertSuggestions("""
                Java<com.zergatul.scripting.tests.completion.JavaTypeTests$MyClass>.#<cursor>
                """,
                context -> suggestions);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }

    @SuppressWarnings("unused")
    public static class MyClass {

        private static int staticField;
        private int field = 100;

        private static void staticMethod() {}

        private int getField() {
            return field;
        }

        private void setField(int value) {
            field = value;
        }
    }
}