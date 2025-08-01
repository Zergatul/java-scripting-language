package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class JavaTypeTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest() {
        String code = """
                Java<java.lang.Object> o = api.getObject();
                intStorage.add(o.hashCode());
                stringStorage.add(o.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(ApiRoot.api.getObject().hashCode()));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(ApiRoot.api.getObject().toString()));
    }

    @Test
    public void staticMembersTest() {
        String code = """
                intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.getInt());
                intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field);
                boolStorage.add(Java<java.util.Objects>.equals("qqq", "qqq"));
                boolStorage.add(Java<java.util.Objects>.equals("qqq", "www"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123456, 654321));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void enumTest() {
        String code = """
                int func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {
                    return e.getValue();
                }
                
                intStorage.add(func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));
                intStorage.add(func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100, 200));
    }

    @Test
    public void compareTest() {
        String code = """
                int func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {
                    return e == Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1 ? 1 : 2;
                }
                
                int func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {
                    return e != Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1 ? 3 : 4;
                }
                
                intStorage.add(func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));
                intStorage.add(func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));
                intStorage.add(func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));
                intStorage.add(func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 3));
    }

    @Test
    public void vectorTest() {
        String code = """
                let vector = new Java<java.util.Vector>();
                vector.add(false);
                vector.add(1);
                vector.add(4000000000L);
                vector.add(api.fromFloat64(1.25));
                vector.add(1.5);
                vector.add('a');
                vector.add("qq");
                
                for (let i = 0; i < vector.size(); i++) {
                    stringStorage.add(vector.get(i).toString());
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("false", "1", "4000000000", "1.25", "1.5", "a", "qq"));
    }

    @Test
    public void hashtableTest() {
        String code = """
                let table = new Java<java.util.Hashtable>();
                table.put(false, 100);
                table.put(200, true);
                table.put("qq", "ww");
                
                stringStorage.add(table.get("qq").toString());
                stringStorage.add(table.get(200).toString());
                stringStorage.add(table.get(false).toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("ww", "true", "100"));
    }

    @Test
    public void boxingTest() {
        String code = """
                let vector = new Java<java.util.Vector>();
                for (let i = 0; i < 10; i++) {
                    vector.add(i);
                }
                
                int sum = 0;
                for (let i = 0; i < vector.size(); i++) {
                    if (vector.get(i) is int) {
                        sum += vector.get(i) as int;
                    } else {
                        intStorage.add(-1);
                    }
                }
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(45));
    }

    @Test
    public void interfaceMethodTest() {
        String code = """
                Java<java.util.List> list = new Java<java.util.Vector>();
                for (let i = 10; i < 16; i++) {
                    list.add(i.toString());
                }
                
                list.addFirst("Q"); // default method
                
                for (let i = 0; i < list.size(); i++) {
                    stringStorage.add(list.get(i) as string);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Q", "10", "11", "12", "13", "14", "15"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    public static class Api {

        private final Object object = new Object();

        public Object getObject() {
            return object;
        }

        public float fromFloat64(double value) {
            return (float) value;
        }
    }

    public static class ClassA {

        public static int field = 654321;

        public static int getInt() {
            return 123456;
        }
    }

    public enum EnumA {
        VAL_1(100),
        VAL_2(200);

        private int value;

        EnumA(int value) {
            this.value  = value;
        }

        public int getValue() {
            return value;
        }
    }
}