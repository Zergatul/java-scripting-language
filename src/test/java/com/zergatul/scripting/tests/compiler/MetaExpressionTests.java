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

public class MetaExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                boolStorage.add(#typeof(false) == #type(boolean));
                boolStorage.add(#typeof(1) == #type(int));
                boolStorage.add(#typeof('a') == #type(char));
                boolStorage.add(#typeof(3000000000L) == #type(int64));
                boolStorage.add(#typeof(0.0) == #type(float));
                boolStorage.add(#typeof("") == #type(string));
                
                boolStorage.add(#typeof(1.1) == #type(int));
                boolStorage.add(#typeof(1.1) != #type(int));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(
                true, true, true, true, true, true,
                false, true));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}