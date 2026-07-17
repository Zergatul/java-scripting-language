package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.analysis.hover.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.tests.utility.CursorHelper;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HoverFactoryTests {

    @Test
    public void predefinedTypeDocumentationTest() {
        HoverInfoFactory factory = new HoverInfoFactory();
        DocumentationProvider documentationProvider = new DocumentationProvider();

        Assertions.assertEquals(
                new HoverInfo("int8", documentationProvider.getTypeDocs(SInt8.instance)),
                factory.getTypeHover(SInt8.instance));
        Assertions.assertEquals(
                new HoverInfo("float32", documentationProvider.getTypeDocs(SFloat32.instance)),
                factory.getTypeHover(SFloat32.instance));
    }

    @Test
    public void customTypeFormatterTest() {
        MethodReference method = SType.fromJavaType(JavaType.class)
                .getInstanceMethods()
                .stream()
                .filter(candidate -> candidate.getName().equals("convert"))
                .findFirst()
                .orElseThrow();
        HoverInfoFactory factory = new HoverInfoFactory(new TypeDisplayFormatter(Class::getSimpleName));

        Assertions.assertEquals(
                "JavaType JavaType.convert(JavaType value)",
                factory.getMethodHover(method).signature());
    }

    @Test
    public void mappedProviderTest() {
        CursorHelper.Result cursor = CursorHelper.parse("let value = <cursor>123;");
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setInterface(Runnable.class)
                .build();
        HoverProvider<String> provider = new HoverProvider<>(
                new MappedHoverFactory<>(HoverInfo::signature));

        HoverProvider.HoverResponse<String> response = provider.get(
                new Analyzer().analyze(cursor.code(), parameters).binderOutput(),
                cursor.line(),
                cursor.column());

        Assertions.assertNotNull(response);
        Assertions.assertEquals("int", response.content());
    }

    @SuppressWarnings("unused")
    public static class JavaType {
        public JavaType convert(JavaType value) {
            return value;
        }
    }

    public static class ApiRoot {}
}