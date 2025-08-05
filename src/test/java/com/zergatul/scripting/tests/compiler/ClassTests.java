package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ClassTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                class Class{}
                
                Class x = new Class();
                stringStorage.add(#typeof(x).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void functionTest() {
        String code = """
                Class func(Class c) { return c; }
                class Class {}
                
                stringStorage.add(#typeof(func(new Class())).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void staticVariableTest() {
        String code = """
                static Class cc = new Class();
                class Class {}
                
                stringStorage.add(#typeof(cc).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void fieldTest() {
        String code = """
                class Class {
                    int x;
                }
                
                Class c = new Class();
                c.x = 123;
                c.x++;
                c.x += 2;
                intStorage.add(c.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(126));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}
