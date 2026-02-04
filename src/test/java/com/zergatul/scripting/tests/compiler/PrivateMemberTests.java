package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Int64Storage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class PrivateMemberTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void instanceFieldReadWriteTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;
                
                let instance = new MyClass(0);
                intStorage.add(instance.#value);
                
                instance.inc();
                intStorage.add(instance.#value);
                
                instance.#value = 100;
                instance.inc();
                intStorage.add(instance.#value);
                
                instance.#value++;
                intStorage.add(instance.#value);
                
                instance.#value += 10;
                intStorage.add(instance.#value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 1, 101, 102, 112));
    }

    @Test
    public void instanceFinalFieldReadTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;
                
                let instance = new MyClass(10);
                intStorage.add(instance.#value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
    }

    @Test
    public void instanceFinalFieldWriteTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;
                
                let instance = new MyClass(10);
                intStorage.add(instance.#value);
                instance.#value = 100;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.ExpressionCannotBeSet, new SingleLineTextRange(5, 1, 159, 15))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void staticFieldReadWriteTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;
                
                stringStorage.add(MyClass.#strValue);
                MyClass.#strValue = "a";
                stringStorage.add(MyClass.#strValue);
                MyClass.#strValue += "c";
                stringStorage.add(MyClass.#strValue);
                
                int64Storage.add(MyClass.#longValue);
                MyClass.#longValue++;
                int64Storage.add(MyClass.#longValue);
                MyClass.#longValue += 10;
                int64Storage.add(MyClass.#longValue);
                """;

        MyClass1.strValue = "q";
        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("q", "a", "ac"));
        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, List.of(100L, 101L, 111L));
    }

    @Test
    public void staticFinalFieldReadTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;
                
                stringStorage.add(MyClass.#strValue);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("a"));
    }

    @Test
    public void staticFinalFieldWriteTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;
                
                MyClass.#strValue = null;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.ExpressionCannotBeSet, new SingleLineTextRange(3, 1, 94, 17))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void staticMethodTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass1>;
                
                stringStorage.add(MyClass.#getStrValue());
                MyClass.#setStrValue("hello");
                stringStorage.add(MyClass.#getStrValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("q", "hello"));
    }

    @Test
    public void instanceMethodTest() {
        String code = """
                typealias MyClass = Java<com.zergatul.scripting.tests.compiler.PrivateMemberTests$MyClass2>;
                
                let instance = new MyClass(10);
                intStorage.add(instance.#getMutableValue());
                instance.#setMutableValue(20);
                intStorage.add(instance.#getMutableValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20));
    }

    @Test
    public void accessDeniedPropertiesTest() {
        String code = """
                typealias Hashtable = Java<java.util.Hashtable>;
                
                let instance = new Hashtable();
                instance.#count = 10;
                Hashtable.#KEYS.toString();
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.PrivateAccessDenied,
                                new SingleLineTextRange(4, 11, 92, 5),
                                "java.lang.IllegalAccessException: module java.base does not open java.util to " + getClass().getModule()),
                        new DiagnosticMessage(
                                BinderErrors.PrivateAccessDenied,
                                new SingleLineTextRange(5, 12, 115, 4),
                                "java.lang.IllegalAccessException: module java.base does not open java.util to " + getClass().getModule())),
                getDiagnostics(RefTests.ApiRoot.class, code));
    }

    @Test
    public void accessDeniedMethodsTest() {
        String code = """
                typealias System = Java<java.lang.System>;
                typealias Hashtable = Java<java.util.Hashtable>;
                
                let instance = new Hashtable();
                instance.#rehash();
                System.#checkIO();
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.PrivateAccessDenied,
                                new SingleLineTextRange(5, 11, 135, 6),
                                "java.lang.IllegalAccessException: module java.base does not open java.util to " + getClass().getModule()),
                        new DiagnosticMessage(
                                BinderErrors.PrivateAccessDenied,
                                new SingleLineTextRange(6, 9, 153, 7),
                                "java.lang.IllegalAccessException: module java.base does not open java.lang to " + getClass().getModule())),
                getDiagnostics(RefTests.ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
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
    }
}