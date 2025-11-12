package com.zergatul.scripting.tests.runtime;

import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.NativeInstanceMethodReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class STypeTests {

    @Test
    public void genericArrayTypeTest() throws Exception {
        Method method = TestClass1.class.getMethod("getParts");
        MethodReference methodReference = new NativeInstanceMethodReference(method);
        SType returnType = methodReference.getReturn();
        Assertions.assertEquals("Java<com.zergatul.scripting.tests.runtime.STypeTests$TestClass2>[]", returnType.toString());
    }

    private static class TestClass1 implements TestInterface {}

    private interface TestInterface {
        default TestClass2<?>[] getParts() {
            return null;
        }
    }

    private static class TestClass2<T> {
        public void log(T value) {
            System.out.println(value);
        }
    }
}