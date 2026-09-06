package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class JavaTypeTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest() {
        String code =
                "Java<java.lang.Object> o = api.getObject();\n" +
                "intStorage.add(o.hashCode());\n" +
                "stringStorage.add(o.toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(ApiRoot.api.getObject().hashCode()), ApiRoot.intStorage.list);
        Assertions.assertIterableEquals(Lists.of(ApiRoot.api.getObject().toString()), ApiRoot.stringStorage.list);
    }

    @Test
    public void staticMembersTest() {
        String code =
                "intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.getInt());\n" +
                "intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field);\n" +
                "boolStorage.add(Java<java.util.Objects>.equals(\"qqq\", \"qqq\"));\n" +
                "boolStorage.add(Java<java.util.Objects>.equals(\"qqq\", \"www\"));\n" +
                "\n" +
                "Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field = 100;\n" +
                "intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field);\n" +
                "Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field += 10;\n" +
                "intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field);\n" +
                "Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field++;\n" +
                "intStorage.add(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$ClassA>.field);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123456, 654321, 100, 110, 111), ApiRoot.intStorage.list);
        Assertions.assertIterableEquals(Lists.of(true, false), ApiRoot.boolStorage.list);
    }

    @Test
    public void enumTest() {
        String code =
                "int func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {\n" +
                "    return e.getValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));\n" +
                "intStorage.add(func(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(100, 200), ApiRoot.intStorage.list);
    }

    @Test
    public void compareTest() {
        String code =
                "int func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {\n" +
                "    return e == Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1 ? 1 : 2;\n" +
                "}\n" +
                "\n" +
                "int func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA> e) {\n" +
                "    return e != Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1 ? 3 : 4;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));\n" +
                "intStorage.add(func1(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));\n" +
                "intStorage.add(func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_1));\n" +
                "intStorage.add(func2(Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$EnumA>.VAL_2));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 4, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void vectorTest() {
        String code =
                "let vector = new Java<java.util.Vector>();\n" +
                "vector.add(false);\n" +
                "vector.add(1);\n" +
                "vector.add(4000000000L);\n" +
                "vector.add(api.fromFloat64(1.25));\n" +
                "vector.add(1.5);\n" +
                "vector.add('a');\n" +
                "vector.add(\"qq\");\n" +
                "\n" +
                "for (let i = 0; i < vector.size(); i++) {\n" +
                "    stringStorage.add(vector.get(i).toString());\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("false", "1", "4000000000", "1.25", "1.5", "a", "qq"), ApiRoot.stringStorage.list);
    }

    @Test
    public void hashtableTest() {
        String code =
                "let table = new Java<java.util.Hashtable>();\n" +
                "table.put(false, 100);\n" +
                "table.put(200, true);\n" +
                "table.put(\"qq\", \"ww\");\n" +
                "\n" +
                "stringStorage.add(table.get(\"qq\").toString());\n" +
                "stringStorage.add(table.get(200).toString());\n" +
                "stringStorage.add(table.get(false).toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("ww", "true", "100"), ApiRoot.stringStorage.list);
    }

    @Test
    public void boxingTest() {
        String code =
                "let vector = new Java<java.util.Vector>();\n" +
                "for (let i = 0; i < 10; i++) {\n" +
                "    vector.add(i);\n" +
                "}\n" +
                "\n" +
                "int sum = 0;\n" +
                "for (let i = 0; i < vector.size(); i++) {\n" +
                "    if (vector.get(i) is int) {\n" +
                "        sum += vector.get(i) as int;\n" +
                "    } else {\n" +
                "        intStorage.add(-1);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(45), ApiRoot.intStorage.list);
    }

    @Test
    public void interfaceMethodTest() {
        String code =
                "Java<java.util.List> list = new Java<java.util.Vector>();\n" +
                "for (let i = 10; i < 16; i++) {\n" +
                "    list.add(i.toString());\n" +
                "}\n" +
                "\n" +
                "list.replaceAll(s => s + \"!\"); // default method\n" +
                "\n" +
                "for (let i = 0; i < list.size(); i++) {\n" +
                "    stringStorage.add(list.get(i) as string);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("10!", "11!", "12!", "13!", "14!", "15!"), ApiRoot.stringStorage.list);
    }

    @Test
    public void functionReturnTypeTest() {
        String code =
                "Java<java.util.Vector>[] func() {\n" +
                "    let list = new Java<java.util.Vector>();\n" +
                "    for (let i = 1; i <= 5; i++) {\n" +
                "        list.add(i);\n" +
                "    }\n" +
                "    return [list];\n" +
                "}\n" +
                "\n" +
                "let list = func()[0];\n" +
                "for (let i = 0; i < list.size(); i++) {\n" +
                "    intStorage.add(list.get(i) as int);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5), ApiRoot.intStorage.list);
    }

    @Test
    public void baseClassFieldTest() {
        String code =
                "let instance = new Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$InheritedClass1>();\n" +
                "instance.enabled = true;\n" +
                "boolStorage.add(instance.enabled);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(true), ApiRoot.boolStorage.list);
    }

    @Test
    public void characterTest() {
        String code =
                "typealias Character = Java<java.lang.Character>;\n" +
                "typealias String = Java<java.lang.String>;\n" +
                "\n" +
                "string test = \"\";\n" +
                "for (int i = 0; i < 6; i++) {\n" +
                "    let ch = Character.toChars('A' + i);\n" +
                "    test += String.valueOf(ch[0]);\n" +
                "}\n" +
                "stringStorage.add(test);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("ABCDEF"), ApiRoot.stringStorage.list);
    }

    @Test
    public void staticInterfaceMethodTest() {
        String code =
                "let instance = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$MyInterface>.getInstance();\n" +
                "stringStorage.add(instance.getName());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("MyName"), ApiRoot.stringStorage.list);
    }

    @Test
    public void functionalInterfaceDirectInvocationTest() {
        String code =
                "let instance = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$MyCallable>.getInstance();\n" +
                "stringStorage.add(instance());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("called"), ApiRoot.stringStorage.list);
    }

    @Test
    public void functionalInterfaceInvalidArgumentsTest() {
        String code =
                "let instance = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$IntCallable>.getInstance();\n" +
                "stringStorage.add(instance⟦(\"text\")⟧);\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.CallableInvalidArguments,
                "string <invocable>(int value)");
    }

    @Test
    public void functionalInterfaceLambdaConversionTest() {
        String code =
                "typealias MyCallable = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$MyCallable>;\n" +
                "MyCallable callable = () => \"lambda\";\n" +
                "stringStorage.add(callable());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("lambda"), ApiRoot.stringStorage.list);
    }

    @Test
    public void functionalInterfaceFunctionGroupConversionTest() {
        String code =
                "typealias MyCallable = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$MyCallable>;\n" +
                "string getValue() => \"function\";\n" +
                "\n" +
                "MyCallable callable = getValue;\n" +
                "stringStorage.add(callable());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("function"), ApiRoot.stringStorage.list);
    }

    @Test
    public void functionalInterfaceMethodGroupConversionTest() {
        String code =
                "typealias MyCallable = Java<com.zergatul.scripting.tests.compiler.JavaTypeTests$MyCallable>;\n" +
                "MyCallable callable = api.getString;\n" +
                "stringStorage.add(callable());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("method"), ApiRoot.stringStorage.list);
    }

    @Test
    public void constructorInvalidArguments() {
        String code =
                "typealias ArrayList = Java<java.util.ArrayList>;\n" +
                "⟦new ArrayList(\"hello\", \"world\")⟧;\n";

        String candidates =
                "Candidates:\n" +
                "constructor Java<java.util.ArrayList>()\n" +
                "constructor Java<java.util.ArrayList>(int arg0)\n" +
                "constructor Java<java.util.ArrayList>(Java<java.util.Collection> arg0)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.NoOverloadedConstructors,
                "Java<java.util.ArrayList>", 2, candidates);
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {

        private final Object object = new Object();

        public Object getObject() {
            return object;
        }

        public float fromFloat64(double value) {
            return (float) value;
        }

        public String getString() {
            return "method";
        }
    }

    @SuppressWarnings("unused")
    public static class ClassA {

        public static int field = 654321;

        public static int getInt() {
            return 123456;
        }
    }

    @SuppressWarnings("unused")
    public enum EnumA {
        VAL_1(100),
        VAL_2(200);

        private final int value;

        EnumA(int value) {
            this.value  = value;
        }

        public int getValue() {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public static class BaseClass1 {
        public boolean enabled;
    }

    @SuppressWarnings("unused")
    public static class InheritedClass1 extends BaseClass1 {
        public int color;
    }

    public interface MyInterface {

        static MyInterface getInstance() {
            return MyInterfaceImpl.INSTANCE;
        }

        String getName();
    }

    public static class MyInterfaceImpl implements MyInterface {

        public static final MyInterfaceImpl INSTANCE = new MyInterfaceImpl();

        private MyInterfaceImpl() {}

        @Override
        public String getName() {
            return "MyName";
        }
    }

    @SuppressWarnings("unused")
    public interface MyCallable {

        static MyCallable getInstance() {
            return () -> "called";
        }

        String invoke();
    }

    @SuppressWarnings("unused")
    public interface IntCallable {

        static IntCallable getInstance() {
            return value -> Integer.toString(value);
        }

        String invoke(int value);
    }
}