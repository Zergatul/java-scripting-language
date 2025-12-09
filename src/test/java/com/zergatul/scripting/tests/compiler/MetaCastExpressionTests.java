package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class MetaCastExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void valueTypeValidCastTest() {
        String code = """
                intStorage.add(#cast(123, int));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
    }

    @Test
    public void valueTypeInvalidCastTest() {
        String code = """
                intStorage.add(#cast(1.0, int));
                """;

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(ClassCastException.class, program::run);
    }

    @Test
    public void referenceTypeValidCastTest() {
        String code = """
                stringStorage.add(#cast("x", string));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("x"));
    }

    @Test
    public void referenceTypeInvalidCastTest() {
        String code = """
                stringStorage.add(#cast(new Java<java.lang.Object>(), string));
                """;

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(ClassCastException.class, program::run);
    }

    @Test
    public void unboxingTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object getInt() => 10;
                
                intStorage.add(#cast(getInt(), int));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
    }

    @Test
    public void boxingTest() {
        String code = """
                typealias Integer = Java<java.lang.Integer>;
                
                intStorage.add(#cast(12, Integer).intValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(12));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}