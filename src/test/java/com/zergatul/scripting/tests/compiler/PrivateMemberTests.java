package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class PrivateMemberTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void instanceFieldTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass>;
                
                let instance = new MyClass(0);
                intStorage.add(instance.#value);
                instance.inc();
                intStorage.add(instance.#value);
                instance.#value = 100;
                instance.inc();
                intStorage.add(instance.#value);
                stringStorage.add(#cast(instance, Object).toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 1, 101));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("101"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
    public static class MyClass {

        private int value;

        public MyClass(int value) {
            this.value = value;
        }

        public void inc() {
            value++;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}