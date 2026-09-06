package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class MetaExpressionTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest() {
        String code =
                "boolStorage.add(#typeof(false) == #type(boolean));\n" +
                "boolStorage.add(#typeof(1) == #type(int));\n" +
                "boolStorage.add(#typeof('a') == #type(char));\n" +
                "boolStorage.add(#typeof(3000000000L) == #type(int64));\n" +
                "boolStorage.add(#typeof(0.0) == #type(float));\n" +
                "boolStorage.add(#typeof(\"\") == #type(string));\n" +
                "\n" +
                "boolStorage.add(#typeof(1.1) == #type(int));\n" +
                "boolStorage.add(#typeof(1.1) != #type(int));\n" +
                "boolStorage.add(#typeof(boolStorage) == #typeof(boolStorage));\n" +
                "boolStorage.add(#typeof(boolStorage) == #typeof(intStorage));\n" +
                "\n" +
                "boolStorage.add(#typeof(api.getA()) == #type(TypeA));\n" +
                "boolStorage.add(#typeof(api.getB()) == #type(TypeB));\n" +
                "boolStorage.add(#typeof(api.getC()) == #type(TypeC));\n" +
                "boolStorage.add(#typeof(api.getB()) == #type(TypeA));\n" +
                "boolStorage.add(#typeof(api.getC()) == #type(TypeA));\n";

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, TypeA.class, TypeB.class, TypeC.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(
                true, true, true, true, true, true,
                false, true, true, false,
                true, true, true, false, false));
    }

    @Test
    public void typeNameTest() {
        String code =
                "stringStorage.add(#typeof(123).name);\n" +
                "stringStorage.add(#type(int).name);\n" +
                "stringStorage.add(#typeof(\"\").name);\n" +
                "stringStorage.add(#type(string).name);\n" +
                "stringStorage.add(#typeof(api.getA()).name);\n" +
                "stringStorage.add(#typeof(api.getB()).name);\n" +
                "stringStorage.add(#typeof(api.getC()).name);\n" +
                "stringStorage.add(#typeof(api).name);\n";

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, TypeA.class, TypeB.class, TypeC.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of(
                "int", "int",
                "string", "string",
                "TypeA", "TypeB", "TypeC",
                "Java<com.zergatul.scripting.tests.compiler.MetaExpressionTests$Api>"));
    }

    @Test
    public void errorTest() {
        String code =
                "let x = #type(1);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(ParserErrors.TypeExpected, new SingleLineTextRange(1, 15, 14, 1), "1")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void synonymsTest() {
        String code =
                "boolStorage.add(#type(int) == #type(int32));\n" +
                "boolStorage.add(#type(long) == #type(int64));\n" +
                "boolStorage.add(#type(float) == #type(float64));\n" +
                "boolStorage.add(#type(int) == #type(int16));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, true, false));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    public static class Api {
        public TypeA getA() { return new TypeA(); }
        public TypeA getB() { return new TypeB(); }
        public TypeA getC() { return new TypeC(); }
    }

    @CustomType(name = "TypeA")
    public static class TypeA {}

    @CustomType(name = "TypeB")
    public static class TypeB extends TypeA {}

    @CustomType(name = "TypeC")
    public static class TypeC extends TypeB {}
}