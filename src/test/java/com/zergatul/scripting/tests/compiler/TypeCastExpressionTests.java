package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import com.zergatul.scripting.utility.Lists;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class TypeCastExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void boolTest() {
        String code =
                "boolStorage.add(false as boolean);\n" +
                "boolStorage.add(true as boolean);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
    }

    @Test
    public void intTest() {
        String code =
                "intStorage.add(0 as int);\n" +
                "intStorage.add(1 as int);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 1));
    }

    @Test
    public void arrayTest() {
        String code =
                "foreach (let val in [1, 2, 3] as int[]) {\n" +
                "    intStorage.add(val);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3));
    }

    @Test
    public void validCastValueTypeTest() {
        String code =
                "long a = 123;\n" +
                "long b = a as long;\n" +
                "int64Storage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, Lists.of(123L));
    }

    @Test
    public void nonValidCastValueTypeTest() {
        String code =
                "let x = 1 as float;\n" +
                "floatStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(0.0));
    }

    @Test
    public void validCastReferenceTypeTest() {
        String code =
                "Java<java.lang.Object> func() => \"123\";\n" +
                "\n" +
                "stringStorage.add(func() as string);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123"));
    }

    @Test
    public void nonValidCastReferenceTypeTest() {
        String code =
                "Java<java.lang.Object> func() => new Java<java.lang.Object>();\n" +
                "\n" +
                "stringStorage.add(func() as string);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Collections.singletonList((String) null));
    }

    @Test
    public void customTypesTest1() {
        String code =
                "let o1 = api.getBaseType();\n" +
                "let o2 = api.getChildType();\n" +
                "if (o1 is ChildType) {\n" +
                "    intStorage.add((o1 as ChildType).getNumber());\n" +
                "} else {\n" +
                "    intStorage.add(100 - (o1 as BaseType).getNumber());\n" +
                "}\n" +
                "if (o2 is ChildType) {\n" +
                "    intStorage.add((o2 as ChildType).getNumber());\n" +
                "    intStorage.add((o2 as ChildType).getCount());\n" +
                "}\n" +
                "intStorage.add(o1.getNumber() + o2.getNumber());\n";

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, BaseType.class, ChildType.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(90, 20, 1, 30));
    }

    @Test
    public void boxedCastTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "typealias Integer = Java<java.lang.Integer>;\n" +
                "\n" +
                "Object getInt() => 10;\n" +
                "\n" +
                "intStorage.add(getInt() as int);\n" +
                "intStorage.add((20 as Integer).intValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {
        public BaseType getBaseType() {
            return new BaseType();
        }
        public BaseType getChildType() {
            return new ChildType();
        }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "BaseType")
    public static class BaseType {
        public int getNumber() { return 10; }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "ChildType")
    public static class ChildType extends BaseType {
        @Override
        public int getNumber() { return 20; }
        public int getCount() { return 1; }
    }
}