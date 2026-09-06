package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ArrayTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.float32Storage = new Float32Storage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.test = new TestApi();
    }

    @Test
    public void initialValueTest() {
        String code =
                "int[] array;\n" +
                "intStorage.add(array.length);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0));
    }

    @Test
    public void simpleTest() {
        String code =
                "int[] data3 = new int[5];\n" +
                "data3[0] = 10;\n" +
                "data3[1] = 20;\n" +
                "data3[2] = 30;\n" +
                "data3[3] = 40;\n" +
                "data3[4] = 50;\n" +
                "intStorage.add(data3[0]);\n" +
                "intStorage.add(data3[1]);\n" +
                "intStorage.add(data3[2]);\n" +
                "intStorage.add(data3[3]);\n" +
                "intStorage.add(data3[4]);\n" +
                "intStorage.add(data3.length);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(10, 20, 30, 40, 50, 5));
    }

    @Test
    public void incrementOperatorTest() {
        String code =
                "int[] array = new int[5];\n" +
                "intStorage.add(array[0]);\n" +
                "array[0]++;\n" +
                "intStorage.add(array[0]);\n" +
                "array[0]++;\n" +
                "intStorage.add(array[0]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0, 1, 2));
    }

    @Test
    public void inlineInitializationTest() {
        String code =
                "int[] data = new int[] { 10, 20, 30, 40, 50 };\n" +
                "intStorage.add(data.length);\n" +
                "intStorage.add(data[0]);\n" +
                "intStorage.add(data[1]);\n" +
                "intStorage.add(data[2]);\n" +
                "intStorage.add(data[3]);\n" +
                "intStorage.add(data[4]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(5, 10, 20, 30, 40, 50));
    }

    @Test
    public void arrayOfArraysTest() {
        String code =
                "int[][] a = new int[][10];\n" +
                "for (int i = 0; i < a.length; i++) {\n" +
                "    a[i] = new int[i + 1];\n" +
                "}\n" +
                "a[5][2]++;\n" +
                "intStorage.add(a.length);\n" +
                "intStorage.add(a[0].length);\n" +
                "intStorage.add(a[1].length);\n" +
                "intStorage.add(a[8].length);\n" +
                "intStorage.add(a[9].length);\n" +
                "intStorage.add(a[5][2]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(10, 1, 2, 9, 10, 1));
    }

    @Test
    public void postfixCallOnceTest() {
        String code =
                "test.getArray()[test.getIndex()]++;\n" +
                "intStorage.add(0);\n" +
                "test.getArray()[test.getIndex()]--;\n" +
                "intStorage.add(test.getArray()[2]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 0xABCD, 30));
    }

    @Test
    public void concatBooleanTest() {
        String code =
                "boolean[] a1 = new boolean[] { false, false, true };\n" +
                "boolean[] a2 = new boolean[] { false, true };\n" +
                "boolean[] a3 = a1 + a2 + false;\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) boolStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(6));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, false, true, false, true, false));
    }

    @Test
    public void concatInt8Test() {
        String code =
                "int8[] a1 = new int8[] { (1).toInt8(), (2).toInt8(), (3).toInt8() };\n" +
                "int8[] a2 = new int8[] { (7).toInt8(), (8).toInt8(), (9).toInt8() };\n" +
                "int8[] a3 = a1 + a2 + (10).toInt8();\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatInt16Test() {
        String code =
                "int16[] a1 = new int16[] { (1).toInt16(), (2).toInt16(), (3).toInt16() };\n" +
                "int16[] a2 = new int16[] { (7).toInt16(), (8).toInt16(), (9).toInt16() };\n" +
                "int16[] a3 = a1 + a2 + (10).toInt16();\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatIntTest() {
        String code =
                "int[] a1 = new int[] { 1, 2, 3 };\n" +
                "int[] a2 = new int[] { 7, 8, 9 };\n" +
                "int[] a3 = a1 + a2 + 10;\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatLongTest() {
        String code =
                "long[] a1 = new long[] { 1, 2, 3 };\n" +
                "long[] a2 = new long[] { 7, 8, 9 };\n" +
                "long[] a3 = a1 + a2 + 10L;\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) int64Storage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7));
        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, Lists.of(1L, 2L, 3L, 7L, 8L, 9L, 10L));
    }

    @Test
    public void concatFloat32Test() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "float32[] a1 = new float32[] { parse(\"0.5\"), 2, 3 };\n" +
                "float32[] a2 = new float32[] { 7, 8, parse(\"9.5\") };\n" +
                "float32[] a3 = a1 + a2 + parse(\"10.0\");\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) float32Storage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7));
        Assertions.assertIterableEquals(ApiRoot.float32Storage.list, Lists.of(0.5f, 2.0f, 3.0f, 7.0f, 8.0f, 9.5f, 10.0f));
    }

    @Test
    public void concatFloatTest() {
        String code =
                "float[] a1 = new float[] { 0.5, 2, 3 };\n" +
                "float[] a2 = new float[] { 7, 8, 9.5 };\n" +
                "float[] a3 = a1 + a2 + 10.0;\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) floatStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(7));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(0.5, 2.0, 3.0, 7.0, 8.0, 9.5, 10.0));
    }

    @Test
    public void concatCharTest() {
        String code =
                "char[] a1 = new char[] { 'q' };\n" +
                "char[] a2 = new char[] { 'w' };\n" +
                "char[] a3 = a1 + a2 + 'e';\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3, 113, 119, 101));
    }

    @Test
    public void concatStringTest() {
        String code =
                "string[] a1 = new string[] { \"aa\", \"qq\" };\n" +
                "string[] a2 = new string[] { \"\", \"!!!\" };\n" +
                "string[] a3 = a1 + a2 + \"tt\";\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "for (int i = 0; i < a3.length; i++) stringStorage.add(a3[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(5));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("aa", "qq", "", "!!!", "tt"));
    }

    @Test
    public void concatInnerArraysTest() {
        String code =
                "let a1 = [[\"a\"], [\"bb\", \"ccc\"]];\n" +
                "let a2 = [[\"dddd\"], [\"1\", \"2\", \"3\", \"4\"], [\"q\"]];\n" +
                "let a3 = a1 + a2 + [\"ww\"];\n" +
                "\n" +
                "intStorage.add(a3.length);\n" +
                "foreach (let array in a3) {\n" +
                "    intStorage.add(array.length);\n" +
                "    foreach (let str in array) {\n" +
                "        stringStorage.add(str);\n" +
                "    }\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(6, 1, 2, 1, 4, 1, 1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("a", "bb", "ccc", "dddd", "1", "2", "3", "4", "q", "ww"));
    }

    @Test
    public void augmentedAssignmentCallOnceTest() {
        String code =
                "test.getArray()[test.getIndex()] += 100;\n" +
                "intStorage.add(0);\n" +
                "intStorage.add(test.getArray()[test.getIndex()]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 130));
    }

    @Test
    public void int64ArrayTest() {
        String code =
                "long[] array = new long[10];\n" +
                "for (int i = 0; i < 10; i++) {\n" +
                "    array[i] = i * 1000000000L;\n" +
                "}\n" +
                "int64Storage.add(array[0]);\n" +
                "int64Storage.add(array[4]);\n" +
                "int64Storage.add(array[5]);\n" +
                "int64Storage.add(array[9]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(0L, 4000000000L, 5000000000L, 9000000000L));
    }

    @Test
    public void collectionExpressionSimpleTest() {
        String code =
                "let a1 = [1];\n" +
                "let a2 = [\"a\", \"b\"];\n" +
                "let a3 = [false, true, true];\n" +
                "let a4 = [0.1, 0.2, 0.3, 0.4];\n" +
                "\n" +
                "intStorage.add(a1.length);\n" +
                "intStorage.add(a2.length);\n" +
                "intStorage.add(a3.length);\n" +
                "intStorage.add(a4.length);\n" +
                "intStorage.add(a1[0]);\n" +
                "stringStorage.add(a2[0]);\n" +
                "stringStorage.add(a2[1]);\n" +
                "boolStorage.add(a3[0]);\n" +
                "boolStorage.add(a3[1]);\n" +
                "boolStorage.add(a3[2]);\n" +
                "floatStorage.add(a4[0]);\n" +
                "floatStorage.add(a4[1]);\n" +
                "floatStorage.add(a4[2]);\n" +
                "floatStorage.add(a4[3]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4, 1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("a", "b"));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true, true));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(0.1, 0.2, 0.3, 0.4));
    }

    @Test
    public void letEmptyCollectionTest() {
        String code =
                "let a1 = [];\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.LetEmptyCollection, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void collectionExpressionCannotInferTypeTest() {
        String code =
                "let a1 = [1, 2.5];\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.CannotInferCollectionExpressionTypes, new SingleLineTextRange(1, 14, 13, 3), "int", 1, "float")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void mixedTypeConcatTest() {
        String code =
                "let a1 = [1, 2, 3];\n" +
                "let a2 = a1 + \"s\";\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.BinaryOperatorNotDefined, new SingleLineTextRange(2, 10, 29, 8), "+", "int[]", "string")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void emptyCollectionValidTest() {
        String code =
                "int[] a1 = [];\n" +
                "float[] a2 = [];\n" +
                "string[] a3 = [];\n" +
                "boolean[] a4 = [];\n" +
                "\n" +
                "stringStorage.add(#typeof(a1).name);\n" +
                "stringStorage.add(#typeof(a2).name);\n" +
                "stringStorage.add(#typeof(a3).name);\n" +
                "stringStorage.add(#typeof(a4).name);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of(
                "int[]",
                "float[]",
                "string[]",
                "boolean[]"));
    }

    @Test
    public void emptyCollectionAsArgumentTest() {
        String code =
                "intStorage.add(test.sum([]));\n" +
                "intStorage.add(test.sum([1]));\n" +
                "intStorage.add(test.sum([2, 3]));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 1, 5));
    }

    @Test
    public void emptyCollectionForEach() {
        String code =
                "foreach (let x in []) {}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.CannotIterate, new SingleLineTextRange(1, 19, 18, 2), "[]")),
                getDiagnostics(ApiRoot.class, code));
    }

//    @Test
//    public void emptyCollectionArrayConcatTest() {
//        String code = """
//                int[] a1 = [1, 2];
//                a1 += [];
//                intStorage.add(a1.length);
//                """;
//
//        Runnable program = compile(ApiRoot.class, code);
//        program.run();
//
//        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
//    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static Float32Storage float32Storage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static TestApi test;
    }

    public static class TestApi {

        private int[] array = new int[] { 10, 20, 30 };

        public int[] getArray() {
            ApiRoot.intStorage.add(0xABCD);
            return array;
        }

        public int getIndex() {
            ApiRoot.intStorage.add(0xCDEF);
            return 2;
        }

        public int sum(int[] array) {
            int sum = 0;
            for (int x : array) {
                sum += x;
            }
            return sum;
        }
    }
}