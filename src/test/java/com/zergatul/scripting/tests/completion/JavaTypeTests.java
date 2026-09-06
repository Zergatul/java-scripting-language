package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.completion.ClassSuggestion;
import com.zergatul.scripting.completion.ClassSuggestionType;
import com.zergatul.scripting.completion.JavaInteropSuggestionProvider;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.JavaTypeNameSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.MethodSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.PropertySuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.type.FieldPropertyReference;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
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
        assertJavaTypeSuggestions("Java<<cursor>", "");
    }

    @Test
    public void partialRootPackageTest() {
        assertJavaTypeSuggestions("Java<a<cursor>", "");
    }

    @Test
    public void rootPackageTest() {
        assertJavaTypeSuggestions("Java<java.<cursor>", "java");
    }

    @Test
    public void nestedPackageTest() {
        assertJavaTypeSuggestions("Java<java.lang.<cursor>", "java.lang");
    }

    @Test
    public void partialClassNameTest() {
        assertJavaTypeSuggestions("Java<java.lang.Str<cursor>", "java.lang");
    }

    @Test
    public void cursorInMiddleOfNameTest() {
        assertJavaTypeSuggestions("Java<java.la<cursor>ng.String>", "java");
    }

    @Test
    public void dollarIsNotSeparatorTest() {
        assertJavaTypeSuggestions("Java<com.example.Outer$In<cursor>", "com.example");
    }

    @Test
    public void noProviderTest() {
        assertSuggestions("Java<<cursor>", context -> Lists.of());
    }

    @Test
    public void publicInstanceMembersTest() {
        List<Suggestion> suggestions = Lists.from(
                Arrays.stream(Hashtable.class.getMethods())
                        .filter(m -> m.getDeclaringClass() != Object.class)
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .filter(m -> !Modifier.isStatic(m.getModifiers()))
                        .map(m -> (Suggestion) MethodSuggestion.getInstance(SType.fromJavaType(Hashtable.class), m)));
        String code =
                "let table = new Java<java.util.Hashtable>();\n" +
                "table.<cursor>\n";
        assertSuggestions(
                code,
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
        String code =
                "Java<java.lang.System>.<cursor>\n";
        assertSuggestions(
                code,
                context -> suggestions);
    }

    @Test
    public void privateInstanceMembersTest() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(PropertySuggestion.getInstance(MyClass.class, "field"));
        suggestions.add(MethodSuggestion.getInstance(MyClass.class, "getField"));
        suggestions.add(MethodSuggestion.getInstance(MyClass.class, "setField"));
        String code =
                "let instance = new Java<com.zergatul.scripting.tests.completion.JavaTypeTests$MyClass>();\n" +
                "instance.#<cursor>\n";
        assertSuggestions(
                code,
                context -> suggestions);
    }

    @Test
    public void privateStaticMembersTest() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(PropertySuggestion.getStatic(MyClass.class, "staticField"));
        suggestions.add(MethodSuggestion.getStatic(MyClass.class, "staticMethod"));
        String code =
                "Java<com.zergatul.scripting.tests.completion.JavaTypeTests$MyClass>.#<cursor>\n";
        assertSuggestions(
                code,
                context -> suggestions);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    private void assertJavaTypeSuggestions(String code, String expectedPrefix) {
        List<String> prefixes = new ArrayList<>();
        List<ClassSuggestion> suggestions = Lists.of(
                new ClassSuggestion("reflect", ClassSuggestionType.PACKAGE),
                new ClassSuggestion("String", ClassSuggestionType.CLASS));
        JavaInteropSuggestionProvider provider = prefix -> {
            prefixes.add(prefix);
            return suggestions;
        };

        CompletionTestHelper.assertSuggestions(
                ApiRoot.class,
                code,
                provider,
                context -> Lists.from(
                        suggestions.stream()
                                .map(JavaTypeNameSuggestion::new)
                                .map(suggestion -> (Suggestion) suggestion)));
        Assertions.assertEquals(Lists.of(expectedPrefix), prefixes);
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