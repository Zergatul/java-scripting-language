package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ParametrizedTests {

    @BeforeEach
    public void clean() {
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void returnTest() {
        String code = """
                stringStorage.add(api.getParametrized().getInstance().getString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("InheritedClass"));
    }

    @Test
    public void parametersTest() {
        String code = """
                let obj = new Java<com.zergatul.scripting.tests.compiler.ParametrizedTests$ParametrizedClass2>();
                let instance1 = new Java<com.zergatul.scripting.tests.compiler.ParametrizedTests$InheritedClass>();
                let instance2 = new Java<com.zergatul.scripting.tests.compiler.ParametrizedTests$SomeClass>();
                stringStorage.add(obj.doSomething(instance1, instance2));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("InheritedClass654"));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
        public static Api api;
    }

    public static class Api {
        public ParametrizedClass<InheritedClass> getParametrized() {
            return new ParametrizedClass<>(new InheritedClass());
        }
    }

    public static abstract class BaseClass {
        public String getString() {
            return getClass().getSimpleName();
        }
    }

    public static class InheritedClass extends BaseClass {}

    public static class SomeClass {
        public int getSomething() {
            return 654;
        }
    }

    public static class ParametrizedClass<T extends BaseClass> {

        private final T instance;

        public ParametrizedClass(T instance) {
            this.instance = instance;
        }

        public T getInstance() {
            return instance;
        }
    }

    public static class ParametrizedClass2<T1 extends BaseClass, T2 extends SomeClass> {
        public String doSomething(T1 instance1, T2 instance2) {
            return instance1.getString() + instance2.getSomething();
        }
    }
}