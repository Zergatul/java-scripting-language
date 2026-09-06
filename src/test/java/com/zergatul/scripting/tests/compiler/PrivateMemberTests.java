package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Float32Storage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.Int16Storage;
import com.zergatul.scripting.tests.compiler.helpers.Int8Storage;
import com.zergatul.scripting.tests.compiler.helpers.Int64Storage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class PrivateMemberTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.float32Storage = new Float32Storage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.int8Storage = new Int8Storage();
        ApiRoot.int16Storage = new Int16Storage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void instanceFieldReadWriteTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;\n" +
                "\n" +
                "let instance = new MyClass(0);\n" +
                "intStorage.add(instance.#value);\n" +
                "\n" +
                "instance.inc();\n" +
                "intStorage.add(instance.#value);\n" +
                "\n" +
                "instance.#value = 100;\n" +
                "instance.inc();\n" +
                "intStorage.add(instance.#value);\n" +
                "\n" +
                "instance.#value++;\n" +
                "intStorage.add(instance.#value);\n" +
                "\n" +
                "instance.#value += 10;\n" +
                "intStorage.add(instance.#value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(0, 1, 101, 102, 112), ApiRoot.intStorage.list);
    }

    @Test
    public void instanceFieldValueTypesTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass3>;\n" +
                "\n" +
                "let instance = new MyClass();\n" +
                "\n" +
                "boolStorage.add(instance.#boolValue);\n" +
                "instance.#boolValue = true;\n" +
                "boolStorage.add(instance.#boolValue);\n" +
                "\n" +
                "int8Storage.add(instance.#int8Value);\n" +
                "instance.#int8Value = (11).toInt8();\n" +
                "int8Storage.add(instance.#int8Value);\n" +
                "\n" +
                "int16Storage.add(instance.#int16Value);\n" +
                "instance.#int16Value = (22).toInt16();\n" +
                "int16Storage.add(instance.#int16Value);\n" +
                "\n" +
                "intStorage.add(instance.#intValue);\n" +
                "instance.#intValue = 33;\n" +
                "intStorage.add(instance.#intValue);\n" +
                "\n" +
                "int64Storage.add(instance.#int64Value);\n" +
                "instance.#int64Value = 44L;\n" +
                "int64Storage.add(instance.#int64Value);\n" +
                "\n" +
                "float32Storage.add(instance.#float32Value);\n" +
                "instance.#float32Value = (15.5).toFloat32();\n" +
                "float32Storage.add(instance.#float32Value);\n" +
                "\n" +
                "floatStorage.add(instance.#floatValue);\n" +
                "instance.#floatValue = 16.5;\n" +
                "floatStorage.add(instance.#floatValue);\n" +
                "\n" +
                "intStorage.add(instance.#charValue);\n" +
                "instance.#charValue = 'b';\n" +
                "intStorage.add(instance.#charValue);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(false, true), ApiRoot.boolStorage.list);
        Assertions.assertIterableEquals(Lists.of((byte) 1, (byte) 11), ApiRoot.int8Storage.list);
        Assertions.assertIterableEquals(Lists.of((short) 2, (short) 22), ApiRoot.int16Storage.list);
        Assertions.assertIterableEquals(Lists.of(3, 33, (int) 'a', (int) 'b'), ApiRoot.intStorage.list);
        Assertions.assertIterableEquals(Lists.of(4L, 44L), ApiRoot.int64Storage.list);
        Assertions.assertIterableEquals(Lists.of(5.5f, 15.5f), ApiRoot.float32Storage.list);
        Assertions.assertIterableEquals(Lists.of(6.5, 16.5), ApiRoot.floatStorage.list);
    }

    @Test
    public void instanceFinalFieldReadTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(10);\n" +
                "intStorage.add(instance.#value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedFieldTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(0);\n" +
                "instance.#protectedValue = 20;\n" +
                "intStorage.add(instance.#protectedValue);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(20), ApiRoot.intStorage.list);
    }

    @Test
    public void packagePrivateFieldTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(0);\n" +
                "instance.#packagePrivateValue = 30;\n" +
                "instance.#packagePrivateValue++;\n" +
                "instance.#packagePrivateValue += 10;\n" +
                "intStorage.add(instance.#packagePrivateValue);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(41), ApiRoot.intStorage.list);
    }

    @Test
    public void instanceFinalFieldWriteTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(10);\n" +
                "intStorage.add(instance.#value);\n" +
                "⟦instance.#value⟧ = 100;\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ExpressionCannotBeSet);
    }

    @Test
    public void staticFieldReadWriteTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;\n" +
                "\n" +
                "stringStorage.add(MyClass.#strValue);\n" +
                "MyClass.#strValue = \"a\";\n" +
                "stringStorage.add(MyClass.#strValue);\n" +
                "MyClass.#strValue += \"c\";\n" +
                "stringStorage.add(MyClass.#strValue);\n" +
                "\n" +
                "int64Storage.add(MyClass.#longValue);\n" +
                "MyClass.#longValue++;\n" +
                "int64Storage.add(MyClass.#longValue);\n" +
                "MyClass.#longValue += 10;\n" +
                "int64Storage.add(MyClass.#longValue);\n";

        MyClass1.strValue = "q";
        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("q", "a", "ac"), ApiRoot.stringStorage.list);
        Assertions.assertIterableEquals(Lists.of(100L, 101L, 111L), ApiRoot.int64Storage.list);
    }

    @Test
    public void staticFinalFieldReadTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "stringStorage.add(MyClass.#strValue);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("a"), ApiRoot.stringStorage.list);
    }

    @Test
    public void staticFinalFieldWriteTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "⟦MyClass.#strValue⟧ = null;\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ExpressionCannotBeSet);
    }

    @Test
    public void staticMethodTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;\n" +
                "\n" +
                "stringStorage.add(MyClass.#getStrValue());\n" +
                "MyClass.#setStrValue(\"hello\");\n" +
                "stringStorage.add(MyClass.#getStrValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("q", "hello"), ApiRoot.stringStorage.list);
    }

    @Test
    public void instanceMethodTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(10);\n" +
                "intStorage.add(instance.#getMutableValue());\n" +
                "instance.#setMutableValue(20);\n" +
                "intStorage.add(instance.#getMutableValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 20), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedMethodTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(0);\n" +
                "instance.#setProtectedValue(8);\n" +
                "intStorage.add(instance.#protectedValue);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(8), ApiRoot.intStorage.list);
    }

    @Test
    public void packagePrivateMethodTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;\n" +
                "\n" +
                "let instance = new MyClass(0);\n" +
                "instance.#addPackagePrivateValue(17);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(17), ApiRoot.intStorage.list);
    }

    @Test
    public void javaInternalFieldsTest() {
        String code =
                "typealias Hashtable = Java<java.util.Hashtable>;\n" +
                "\n" +
                "let instance = new Hashtable();\n" +
                "instance.#count = 10;\n" +
                "intStorage.add(instance.#count);\n" +
                "intStorage.add(Hashtable.#KEYS);\n" +
                "intStorage.add(Hashtable.#VALUES);\n" +
                "intStorage.add(Hashtable.#ENTRIES);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 0, 1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void javaInternalMethodsTest() {
        String code =
                "typealias System = Java<java.lang.System>;\n" +
                "typealias Hashtable = Java<java.util.Hashtable>;\n" +
                "\n" +
                "let instance = new Hashtable();\n" +
                "instance.put(\"\", \"\");\n" +
                "instance.#loadFactor = (2.5).toFloat32();\n" +
                "instance.#rehash();\n" +
                "intStorage.add(instance.#threshold);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(57), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedMethodsTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass4>;\n" +
                "\n" +
                "let instance = new MyClass();\n" +
                "intStorage.add(instance.#outer(instance.#inner()));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(5776), ApiRoot.intStorage.list);
    }

    @Test
    public void exceptionTest() {
        String code =
                "typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass5>;\n" +
                "\n" +
                "let instance = new MyClass();\n" +
                "instance.#exception();\n";

        Runnable program = compile(ApiRoot.class, code);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, program::run);
        Assertions.assertEquals("Hello", exception.getMessage());
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Float32Storage float32Storage;
        public static FloatStorage floatStorage;
        public static Int8Storage int8Storage;
        public static Int16Storage int16Storage;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
    public static class MyClass1 {

        private static String strValue = "q";
        private static long longValue = 100;
        private int value;

        public MyClass1(int value) {
            this.value = value;
        }

        private static String getStrValue() {
            return strValue;
        }

        private static void setStrValue(String value) {
            strValue = value;
        }

        public void inc() {
            value++;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    @SuppressWarnings("unused")
    public static class MyClass2 {

        private static final String strValue = "a";
        private static final long longValue = 100;
        private final int value;
        private int mutableValue;
        protected int protectedValue;
        int packagePrivateValue;

        public MyClass2(int value) {
            this.value = value;
            this.mutableValue = value;
        }

        public int getValue() {
            return value;
        }

        private int getMutableValue() {
            return mutableValue;
        }

        private void setMutableValue(int value) {
            mutableValue = value;
        }

        protected void setProtectedValue(int value) {
            protectedValue = value;
        }

        void addPackagePrivateValue(int value) {
            ApiRoot.intStorage.add(value);
        }
    }

    @SuppressWarnings("unused")
    public static class MyClass3 {

        private boolean boolValue;
        private byte int8Value = 1;
        private short int16Value = 2;
        private int intValue = 3;
        private long int64Value = 4;
        private float float32Value = 5.5f;
        private double floatValue = 6.5;
        private char charValue = 'a';
    }

    @SuppressWarnings("unused")
    public static class MyClass4 {

        private int inner() {
            return 76;
        }

        private int outer(int value) {
            return value * value;
        }
    }

    @SuppressWarnings("unused")
    public static class MyClass5 {
        private void exception() {
            throw new RuntimeException("Hello");
        }
    }
}