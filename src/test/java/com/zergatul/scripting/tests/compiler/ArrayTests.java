package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ArrayTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
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
                boolean[] a3 = a1 + a2;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) boolStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5));
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, false, true, false, true));
    }

    @Test
    public void concatIntTest() {
        String code = """
                int[] a1 = new int[] { 1, 2, 3 };
                int[] a2 = new int[] { 7, 8, 9 };
                int[] a3 = a1 + a2;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(6, 1, 2, 3, 7, 8, 9));
    }

    @Test
    public void concatFloatTest() {
        String code = """
                float[] a1 = new float[] { 0.5, 2, 3 };
                float[] a2 = new float[] { 7, 8, 9.5 };
                float[] a3 = a1 + a2;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) floatStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(6));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(0.5, 2.0, 3.0, 7.0, 8.0, 9.5));
    }

    @Test
    public void concatCharTest() {
        String code = """
                char[] a1 = new char[] { 'q' };
                char[] a2 = new char[] { 'w' };
                char[] a3 = a1 + a2;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) intStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 113, 119));
    }

    @Test
    public void concatStringTest() {
        String code = """
                string[] a1 = new string[] { "aa", "qq" };
                string[] a2 = new string[] { "", "!!!" };
                string[] a3 = a1 + a2;
                
                intStorage.add(a3.length);
                for (int i = 0; i < a3.length; i++) stringStorage.add(a3[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(4));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("aa", "qq", "", "!!!"));
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
                List.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 100));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static TestApi test = new TestApi();
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
    }
}