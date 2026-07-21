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
        MethodReference method = MemberLookup.getMethods(SType.fromJavaType(JavaType.class)).stream()
                .filter(candidate -> !candidate.isStatic())
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

    @Test
    public void syntheticInterfaceTest() {
        Assertions.assertEquals(
                "{ string toString() }",
                typeFormatter.format(SStringConvertible.instance));
    }

    @Test
    public void functionalInterfaceTest() {
        Assertions.assertEquals(
                "fn<() => void>",
                typeFormatter.format(SType.fromJavaType(Runnable.class)));

        Assertions.assertEquals(
                "fn<JavaInterface => JavaInterface>",
                typeFormatter.format(SType.fromJavaType(JavaInterface.class)));

        Assertions.assertEquals(
                "fn<() => fn<() => JavaInterfaceA>>",
                typeFormatter.format(SType.fromJavaType(JavaInterfaceA.class)));
    }

    @SuppressWarnings("unused")
    public static class JavaType {
        public JavaType convert(JavaType value) {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public interface JavaInterface {
        JavaInterface convert(JavaInterface value);
    }

    @SuppressWarnings("unused")
    public interface JavaInterfaceA {
        JavaInterfaceB convert();
    }

    @SuppressWarnings("unused")
    public interface JavaInterfaceB {
        JavaInterfaceA convert();
    }
}