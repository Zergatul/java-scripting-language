package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        String code = """
                int[] array;
                intStorage.add(array.length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0));
    }

    @Test
    public void simpleTest() {
        String code = """
                int[] data3 = new int[5];
                data3[0] = 10;
                data3[1] = 20;
                data3[2] = 30;
                data3[3] = 40;
                data3[4] = 50;
                intStorage.add(data3[0]);
                intStorage.add(data3[1]);
                intStorage.add(data3[2]);
                intStorage.add(data3[3]);
                intStorage.add(data3[4]);
                intStorage.add(data3.length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10, 20, 30, 40, 50, 5));
    }

    @Test
    public void incrementOperatorTest() {
        String code = """
                int[] array = new int[5];
                intStorage.add(array[0]);
                array[0]++;
                intStorage.add(array[0]);
                array[0]++;
                intStorage.add(array[0]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0, 1, 2));
    }

    @Test
    public void inlineInitializationTest() {
        String code = """
                int[] data = new int[] { 10, 20, 30, 40, 50 };
                intStorage.add(data.length);
                intStorage.add(data[0]);
                intStorage.add(data[1]);
                intStorage.add(data[2]);
                intStorage.add(data[3]);
                intStorage.add(data[4]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(5, 10, 20, 30, 40, 50));
    }

    @Test
    public void arrayOfArraysTest() {
        String code = """
                int[][] a = new int[][10];
                for (int i = 0; i < a.length; i++) {
                    a[i] = new int[i + 1];
                }
                a[5][2]++;
                intStorage.add(a.length);
                intStorage.add(a[0].length);
                intStorage.add(a[1].length);
                intStorage.add(a[8].length);
                intStorage.add(a[9].length);
                intStorage.add(a[5][2]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10, 1, 2, 9, 10, 1));
    }

    @Test
    public void postfixCallOnceTest() {
        String code = """
                test.getArray()[test.getIndex()]++;
                intStorage.add(0);
                test.getArray()[test.getIndex()]--;
                intStorage.add(test.getArray()[2]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 0xABCD, 30));
    }

    @Test
    public void concatBooleanTest() {
        String code = """
                boolean[] a1 = new boolean[] { false, false, true };
                boolean[] a2 = new boolean[] { false, true };
                boolean[] a3 = a1 + a2 + false;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) boolStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(6));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, false, true, false, true, false));
    }

    @Test
    public void concatInt8Test() {
        String code = """
                int8[] a1 = new int8[] { (1).toInt8(), (2).toInt8(), (3).toInt8() };
                int8[] a2 = new int8[] { (7).toInt8(), (8).toInt8(), (9).toInt8() };
                int8[] a3 = a1 + a2 + (10).toInt8();
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatInt16Test() {
        String code = """
                int16[] a1 = new int16[] { (1).toInt16(), (2).toInt16(), (3).toInt16() };
                int16[] a2 = new int16[] { (7).toInt16(), (8).toInt16(), (9).toInt16() };
                int16[] a3 = a1 + a2 + (10).toInt16();
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatIntTest() {
        String code = """
                int[] a1 = new int[] { 1, 2, 3 };
                int[] a2 = new int[] { 7, 8, 9 };
                int[] a3 = a1 + a2 + 10;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7, 1, 2, 3, 7, 8, 9, 10));
    }

    @Test
    public void concatLongTest() {
        String code = """
                long[] a1 = new long[] { 1, 2, 3 };
                long[] a2 = new long[] { 7, 8, 9 };
                long[] a3 = a1 + a2 + 10L;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) int64Storage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7));
        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, List.of(1L, 2L, 3L, 7L, 8L, 9L, 10L));
    }

    @Test
    public void concatFloat32Test() {
        String code = """
                float32 parse(string s) {
                    float32 f;
                    float32.tryParse(s, ref f);
                    return f;
                }
                
                float32[] a1 = new float32[] { parse("0.5"), 2, 3 };
                float32[] a2 = new float32[] { 7, 8, parse("9.5") };
                float32[] a3 = a1 + a2 + parse("10.0");
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) float32Storage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7));
        Assertions.assertIterableEquals(ApiRoot.float32Storage.list, List.of(0.5f, 2.0f, 3.0f, 7.0f, 8.0f, 9.5f, 10.0f));
    }

    @Test
    public void concatFloatTest() {
        String code = """
                float[] a1 = new float[] { 0.5, 2, 3 };
                float[] a2 = new float[] { 7, 8, 9.5 };
                float[] a3 = a1 + a2 + 10.0;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) floatStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(7));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(0.5, 2.0, 3.0, 7.0, 8.0, 9.5, 10.0));
    }

    @Test
    public void concatCharTest() {
        String code = """
                char[] a1 = new char[] { 'q' };
                char[] a2 = new char[] { 'w' };
                char[] a3 = a1 + a2 + 'e';
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3, 113, 119, 101));
    }

    @Test
    public void concatStringTest() {
        String code = """
                string[] a1 = new string[] { "aa", "qq" };
                string[] a2 = new string[] { "", "!!!" };
                string[] a3 = a1 + a2 + "tt";
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) stringStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("aa", "qq", "", "!!!", "tt"));
    }

    @Test
    public void concatInnerArraysTest() {
        String code = """
                let a1 = [["a"], ["bb", "ccc"]];
                let a2 = [["dddd"], ["1", "2", "3", "4"], ["q"]];
                let a3 = a1 + a2 + ["ww"];
                
                intStorage.add(a3.length);
                foreach (let array in a3) {
                    intStorage.add(array.length);
                    foreach (let str in array) {
                        stringStorage.add(str);
                    }
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(6, 1, 2, 1, 4, 1, 1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("a", "bb", "ccc", "dddd", "1", "2", "3", "4", "q", "ww"));
    }

    @Test
    public void augmentedAssignmentCallOnceTest() {
        String code = """
                test.getArray()[test.getIndex()] += 100;
                intStorage.add(0);
                intStorage.add(test.getArray()[test.getIndex()]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 130));
    }

    @Test
    public void int64ArrayTest() {
        String code = """
                long[] array = new long[10];
                for (int i = 0; i < 10; i++) {
                    array[i] = i * 1000000000L;
                }
                int64Storage.add(array[0]);
                int64Storage.add(array[4]);
                int64Storage.add(array[5]);
                int64Storage.add(array[9]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(0L, 4000000000L, 5000000000L, 9000000000L));
    }

    @Test
    public void collectionExpressionSimpleTest() {
        String code = """
                let a1 = [1];
                let a2 = ["a", "b"];
                let a3 = [false, true, true];
                let a4 = [0.1, 0.2, 0.3, 0.4];
                
                intStorage.add(a1.length);
                intStorage.add(a2.length);
                intStorage.add(a3.length);
                intStorage.add(a4.length);
                intStorage.add(a1[0]);
                stringStorage.add(a2[0]);
                stringStorage.add(a2[1]);
                boolStorage.add(a3[0]);
                boolStorage.add(a3[1]);
                boolStorage.add(a3[2]);
                floatStorage.add(a4[0]);
                floatStorage.add(a4[1]);
                floatStorage.add(a4[2]);
                floatStorage.add(a4[3]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("a", "b"));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true, true));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(0.1, 0.2, 0.3, 0.4));
    }

    @Test
    public void letEmptyCollectionTest() {
        String code = """
                let a1 = [];
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.LetEmptyCollection, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void collectionExpressionCannotInferTypeTest() {
        String code = """
                let a1 = [1, 2.5];
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.CannotInferCollectionExpressionTypes, new SingleLineTextRange(1, 14, 13, 3), "int", 1, "float")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void mixedTypeConcatTest() {
        String code = """
                let a1 = [1, 2, 3];
                let a2 = a1 + "s";
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.BinaryOperatorNotDefined, new SingleLineTextRange(2, 10, 29, 8), "+", "int[]", "string")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void emptyCollectionValidTest() {
        String code = """
                int[] a1 = [];
                float[] a2 = [];
                string[] a3 = [];
                boolean[] a4 = [];
                
                stringStorage.add(#typeof(a1).name);
                stringStorage.add(#typeof(a2).name);
                stringStorage.add(#typeof(a3).name);
                stringStorage.add(#typeof(a4).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(
                "int[]",
                "float[]",
                "string[]",
                "boolean[]"));
    }

    @Test
    public void emptyCollectionAsArgumentTest() {
        String code = """
                intStorage.add(test.sum([]));
                intStorage.add(test.sum([1]));
                intStorage.add(test.sum([2, 3]));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 1, 5));
    }

    @Test
    public void emptyCollectionForEach() {
        String code = """
                foreach (let x in []) {}
                """;

        comparator.assertEquals(List.of(
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
//        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
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