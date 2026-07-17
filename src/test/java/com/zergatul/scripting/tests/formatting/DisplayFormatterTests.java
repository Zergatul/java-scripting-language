package com.zergatul.scripting.tests.formatting;

import com.zergatul.scripting.formatting.MethodSignatureFormatter;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DisplayFormatterTests {

    private final TypeDisplayFormatter typeFormatter = new TypeDisplayFormatter(Class::getSimpleName);

    @Test
    public void typeTest() {
        SType javaType = SClassType.create(JavaType.class);

        Assertions.assertEquals("JavaType", typeFormatter.format(javaType));
        Assertions.assertEquals("JavaType[]", typeFormatter.format(new SArrayType(javaType)));
        Assertions.assertEquals("Future<JavaType>", typeFormatter.format(new SFuture(javaType)));
        Assertions.assertEquals(
                "fn<JavaType => JavaType>",
                typeFormatter.format(new SStaticFunction(
                        javaType,
                        new MethodParameter[] { new MethodParameter("value", javaType) })));
    }

    @Test
    public void methodSignatureTest() {
        MethodReference method = SType.fromJavaType(JavaType.class)
                .getInstanceMethods()
                .stream()
                .filter(candidate -> candidate.getName().equals("convert"))
                .findFirst()
                .orElseThrow();
        MethodSignatureFormatter formatter = new MethodSignatureFormatter(typeFormatter);

        Assertions.assertEquals(
                "JavaType JavaType.convert(JavaType value)",
                formatter.format(method));
        Assertions.assertEquals(
                "(JavaType value)",
                formatter.formatParameters(method.getParameters()));
    }

    @SuppressWarnings("unused")
    public static class JavaType {
        public JavaType convert(JavaType value) {
            return value;
        }
    }
}