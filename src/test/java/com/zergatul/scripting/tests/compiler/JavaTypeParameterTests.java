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

        Assertions.assertIterableEquals(List.of(9, 8), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedExactTest1() {
        String code = """
                typealias List = Java<java.util.List>;
                typealias ArrayList = Java<java.util.ArrayList>;
                
                List<int> of(int[] values) {
                    let result = new ArrayList<int>();
                    foreach (let value in values) {
                        result.add(value);
                    }
                    return result;
                }
                
                let list = new ArrayList<List<int>>();
                list.add(of([1]));
                list.add(of([2, 3]));
                list.add(of([4, 5, 6]));
                intStorage.add(list.get(0).get(0) + list.get(1).get(1) + list.get(2).get(2));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(10), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}