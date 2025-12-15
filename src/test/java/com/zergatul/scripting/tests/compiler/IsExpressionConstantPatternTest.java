package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class IsExpressionConstantPatternTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void nullTest() {
        String code = """
                string str1 = null;
                boolStorage.add(str1 is null);
                boolStorage.add(str1 is not null);
                boolStorage.add(str1 is not not null);
                boolStorage.add(str1 is not not not null);
                string str2;
                boolStorage.add(str2 is null);
                boolStorage.add(str2 is not null);
                boolStorage.add(str2 is not not null);
                boolStorage.add(str2 is not not not null);
                int x;
                boolStorage.add(x is null);
                boolStorage.add(x is not null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        true, false, true, false,
                        false, true, false, true,
                        false, true));
    }

    @Test
    public void boolLiteralTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                typealias Boolean = Java<java.lang.Boolean>;
                
                let b1 = false;
                boolStorage.add(b1 is false);
                boolStorage.add(b1 is not false);
                boolStorage.add(b1 is not not false);
                boolStorage.add(b1 is true);
                boolStorage.add(b1 is not true);
                boolStorage.add(b1 is not not true);
                let b2 = true;
                boolStorage.add(b2 is false);
                boolStorage.add(b2 is not false);
                boolStorage.add(b2 is not not false);
                boolStorage.add(b2 is true);
                boolStorage.add(b2 is not true);
                boolStorage.add(b2 is not not true);
                let str = "";
                boolStorage.add(str is false);
                boolStorage.add(str is not false);
                boolStorage.add(str is true);
                boolStorage.add(str is not true);
                Object boxed1 = Boolean.FALSE;
                boolStorage.add(boxed1 is false);
                boolStorage.add(boxed1 is true);
                Object boxed2 = Boolean.TRUE;
                boolStorage.add(boxed2 is false);
                boolStorage.add(boxed2 is true);
                Object obj = null;
                boolStorage.add(obj is false);
                boolStorage.add(obj is true);
                int x = 1;
                boolStorage.add(x is false);
                boolStorage.add(x is true);
                boolStorage.add(x is not false);
                boolStorage.add(x is not true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        true, false, true, false, true, false,
                        false, true, false, true, false, true,
                        false, true, false, true,
                        true, false,
                        false, true,
                        false, false,
                        false, false, true, true));
    }

    @Test
    public void int32LiteralTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                int i1 = 100;
                boolStorage.add(i1 is 100);
                boolStorage.add(i1 is not 99);
                boolStorage.add(i1 is not 100);
                
                string str = "";
                boolStorage.add(str is 100);
                boolStorage.add(str is not 100);
                
                Object boxed = 50;
                boolStorage.add(boxed is 50);
                boolStorage.add(boxed is not 49);
                boolStorage.add(boxed is not 50);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void int64LiteralTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                long i1 = 100;
                boolStorage.add(i1 is 100L);
                boolStorage.add(i1 is not 99L);
                boolStorage.add(i1 is not 100L);
                
                string str = "";
                boolStorage.add(str is 100L);
                boolStorage.add(str is not 100L);
                
                Object boxed = 50L;
                boolStorage.add(boxed is 50L);
                boolStorage.add(boxed is not 49L);
                boolStorage.add(boxed is not 50L);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void float64LiteralTest() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                float f1 = 100;
                boolStorage.add(f1 is 100.0);
                boolStorage.add(f1 is not 99.0);
                boolStorage.add(f1 is not 100.0);
                
                string str = "";
                boolStorage.add(str is 100.0);
                boolStorage.add(str is not 100.0);
                
                Object boxed = 50.0;
                boolStorage.add(boxed is 50.0);
                boolStorage.add(boxed is not 49.0);
                boolStorage.add(boxed is not 50.0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void stringLiteralTest() {}

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
    }
}