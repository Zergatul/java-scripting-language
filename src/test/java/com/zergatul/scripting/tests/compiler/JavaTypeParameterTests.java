package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class JavaTypeParameterTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.api = new Api();
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

    @Test
    public void methodReturnAndParameterTest() {
        String code = """
                typealias ArrayList = Java<java.util.ArrayList>;

                let values = new ArrayList<int>();
                values.add(10);
                values.add(20);
                intStorage.add(api.sum(values));

                let names = api.getNames();
                intStorage.add(names.get(0).length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(30, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void parameterizedFieldTest() {
        String code = """
                intStorage.add(api.names.get(1).length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(3), ApiRoot.intStorage.list);
    }

    @Test
    public void inheritedTypeParameterTest() {
        String code = """
                let value = api.getDerived();
                intStorage.add(value.getNumber() + value.getNumber());
                intStorage.add(value.getValue().length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(14, 5), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedParameterizedMethodReturnTest() {
        String code = """
                let groups = api.getGroups();
                intStorage.add(groups.get("first").get(1));
                intStorage.add(groups.get("second").get(0));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(20, 30), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {

        public final List<String> names = List.of("one", "two");

        public int sum(List<Integer> values) {
            return values.get(0) + values.get(1);
        }

        public List<String> getNames() {
            return names;
        }

        public Map<String, List<Integer>> getGroups() {
            Map<String, List<Integer>> result = new HashMap<>();
            result.put("first", List.of(10, 20));
            result.put("second", List.of(30, 40));
            return result;
        }

        public Derived getDerived() {
            return new Derived();
        }
    }

    @SuppressWarnings("unused")
    public interface GenericProvider<T> {
        T getNumber();
    }

    @SuppressWarnings("unused")
    public static class GenericBase<T> {

        private final T value;

        protected GenericBase(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public static class Derived extends GenericBase<String> implements GenericProvider<Integer> {

        public Derived() {
            super("hello");
        }

        @Override
        public Integer getNumber() {
            return 7;
        }
    }
}