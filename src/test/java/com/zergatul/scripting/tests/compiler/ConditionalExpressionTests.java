package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ConditionalExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                int x = 123;
                int y = 234;
                intStorage.add(x < y ? 100 : 200);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
    }

    @Test
    public void rightAssociativityTest() {
        String code = """
                boolean a() { intStorage.add(1); return true; }
                boolean b() { intStorage.add(2); return true; }
                boolean c() { intStorage.add(3); return false; }
                boolean d() { intStorage.add(4); return false; }
                boolean e() { intStorage.add(5); return true; }
                
                boolStorage.add((a() ? b() : c()) ? d() : e());
                intStorage.add(0);
                boolStorage.add(a() ? b() : (c() ? d() : e()));
                intStorage.add(0);
                boolStorage.add(a() ? b() : c() ? d() : e());
                intStorage.add(0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true, true));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(
                1, 2, 4, 0,
                1, 2, 0,
                1, 2, 0));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
    }
}