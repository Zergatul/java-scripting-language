package com.zergatul.scripting.tests.runtime;

import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.NativeInstanceMethodReference;
import com.zergatul.scripting.type.SClassType;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

public class STypeTests {

    @Test
    public void genericArrayTypeTest() throws Exception {
        Method method = TestClass1.class.getMethod("getParts");
        MethodReference methodReference = new NativeInstanceMethodReference(method);
        SType returnType = methodReference.getReturn();
        Assertions.assertEquals("Java<com.zergatul.scripting.tests.runtime.STypeTests$TestClass2>[]", returnType.toString());
    }

    @Test
    public void methodTest1() throws Exception {
        Method method = TestClass3.class.getMethod("getPlayers", Predicate.class);
        MethodReference methodReference = new NativeInstanceMethodReference(method);
        SType parameterType = methodReference.getParameterTypes().getFirst();
        Assertions.assertEquals("fn<Java<com.zergatul.scripting.tests.runtime.STypeTests$ServerPlayer> => boolean>", parameterType.toString());
    }

    @Test
    public void notFunctionalInterfaceTest() {
        SType type = SType.fromJavaType(ReputationEventType.class);
        if (!(type instanceof SClassType)) {
            Assertions.fail();
            return;
        }

        Assertions.assertEquals("Java<com.zergatul.scripting.tests.runtime.STypeTests$ReputationEventType>", type.toString());
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

    private static class TestClass3 {
        public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
            return List.of();
        }
    }

    public interface ReputationEventType {
        ReputationEventType TYPE_1 = register("type1");
        ReputationEventType TYPE_2 = register("type2");

        static ReputationEventType register(final String type) {
            return new ReputationEventType() {
                @Override
                public String toString() {
                    return type;
                }
            };
        }
    }

    public static class ServerPlayer {}
}