package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.completion.*;
import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SuggestionInfoFactoryTests {

    private final SuggestionInfoFactory factory = new SuggestionInfoFactory();
    private final DocumentationProvider documentationProvider = new DocumentationProvider();

    @Test
    public void keywordTest() {
        Assertions.assertEquals(
                new SuggestionInfo("#typeof", null, null, "#typeof", SuggestionKind.KEYWORD),
                factory.getKeywordSuggestion(TokenType.META_TYPE_OF));
        Assertions.assertEquals(
                new SuggestionInfo("while", null, null, "while", SuggestionKind.KEYWORD),
                factory.getKeywordSuggestion(TokenType.WHILE));
    }

    @Test
    public void predefinedTypesTest() {
        Assertions.assertEquals(
                List.of(
                        new SuggestionInfo("int", null, documentationProvider.getTypeDocs(SInt.instance), "int", SuggestionKind.TYPE),
                        new SuggestionInfo("int32", null, documentationProvider.getTypeDocs(SInt.instance), "int32", SuggestionKind.TYPE)),
                factory.getTypeSuggestion(SInt.instance));
        Assertions.assertEquals(
                List.of(
                        new SuggestionInfo("long", null, documentationProvider.getTypeDocs(SInt64.instance), "long", SuggestionKind.TYPE),
                        new SuggestionInfo("int64", null, documentationProvider.getTypeDocs(SInt64.instance), "int64", SuggestionKind.TYPE)),
                factory.getTypeSuggestion(SInt64.instance));
        Assertions.assertEquals(
                List.of(
                        new SuggestionInfo("float", null, documentationProvider.getTypeDocs(SFloat.instance), "float", SuggestionKind.TYPE),
                        new SuggestionInfo("float64", null, documentationProvider.getTypeDocs(SFloat.instance), "float64", SuggestionKind.TYPE)),
                factory.getTypeSuggestion(SFloat.instance));
    }

    @Test
    public void memberDocumentationTest() {
        SType type = SType.fromJavaType(TestType.class);
        PropertyReference property = MemberLookup.getProperties(type).stream()
                .filter(candidate -> !candidate.isStatic())
                .filter(candidate -> candidate.getName().equals("value"))
                .findFirst()
                .orElseThrow();
        MethodReference method = MemberLookup.getMethods(type).stream()
                .filter(candidate -> !candidate.isStatic())
                .filter(candidate -> candidate.getName().equals("parse"))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(
                new SuggestionInfo("value", "int", "Value documentation.", "value", SuggestionKind.PROPERTY),
                factory.getPropertySuggestion(property));
        Assertions.assertEquals(
                new SuggestionInfo(
                        "parse",
                        "boolean TestType.parse(string text)",
                        "Method documentation.",
                        "parse",
                        SuggestionKind.METHOD),
                factory.getMethodSuggestion(method));
    }

    @Test
    public void mappedFactoryTest() {
        MappedSuggestionFactory<String> mapped = new MappedSuggestionFactory<>(
                suggestion -> suggestion.kind() + ":" + suggestion.label());

        Assertions.assertEquals("KEYWORD:return", mapped.getKeywordSuggestion(TokenType.RETURN));
        Assertions.assertEquals(
                List.of("TYPE:int", "TYPE:int32"),
                mapped.getTypeSuggestion(SInt.instance));
    }

    @Test
    public void customTypeFormatterTest() {
        MethodReference method = MemberLookup.getMethods(SType.fromJavaType(JavaType.class)).stream()
                .filter(candidate -> !candidate.isStatic())
                .filter(candidate -> candidate.getName().equals("convert"))
                .findFirst()
                .orElseThrow();
        SuggestionInfoFactory factory = new SuggestionInfoFactory(new TypeDisplayFormatter(Class::getSimpleName));

        Assertions.assertEquals(
                "JavaType JavaType.convert(JavaType value)",
                factory.getMethodSuggestion(method).detail());
    }

    @Test
    public void functionTest() {
        Function function = new Function(
                "func",
                new SStaticFunction(
                        SType.fromJavaType(JavaType.class),
                        new MethodParameter[] { new MethodParameter("input", SType.fromJavaType(JavaType.class)) }),
                TextRange.MISSING);
        SuggestionInfoFactory factory = new SuggestionInfoFactory(new TypeDisplayFormatter(Class::getSimpleName));

        Assertions.assertEquals(
                "JavaType func(JavaType input)",
                factory.getFunctionSuggestion(function).detail());
    }

    @SuppressWarnings("unused")
    @CustomType(name = "TestType")
    public static class TestType {

        @PropertyDescription("Value documentation.")
        public int value;

        @MethodDescription("Method documentation.")
        public boolean parse(String text) {
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static class JavaType {
        public JavaType convert(JavaType value) {
            return value;
        }
    }
}