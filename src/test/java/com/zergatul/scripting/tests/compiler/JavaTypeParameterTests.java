package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class JavaTypeParameterTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void exactTest1() {
        String code = """
                typealias ArrayList = Java<java.util.ArrayList>;
                
                let list = new ArrayList<int>();
                list.add(3);
                list.add(4);
                list.add(5);
                intStorage.add(list.get(1) + list.get(2));
                intStorage.add(list.getFirst() + list.getLast());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                List.of(9, 8),
                ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}