package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ArrayTests {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new IntStorage();
    }

    @Test
    public void initialValueTest() {
        String code = """
                int[] array;
                storage.add(array.length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
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
                storage.add(data3[0]);
                storage.add(data3[1]);
                storage.add(data3[2]);
                storage.add(data3[3]);
                storage.add(data3[4]);
                storage.add(data3.length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(10, 20, 30, 40, 50, 5));
    }

    @Test
    public void incrementOperatorTest() {
        String code = """
                int[] array = new int[5];
                storage.add(array[0]);
                array[0]++;
                storage.add(array[0]);
                array[0]++;
                storage.add(array[0]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(0, 1, 2));
    }

    @Test
    public void inlineInitializationTest() {
        String code = """
                int[] data = new int[] { 10, 20, 30, 40, 50 };
                storage.add(data.length);
                storage.add(data[0]);
                storage.add(data[1]);
                storage.add(data[2]);
                storage.add(data[3]);
                storage.add(data[4]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
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
                storage.add(a.length);
                storage.add(a[0].length);
                storage.add(a[1].length);
                storage.add(a[8].length);
                storage.add(a[9].length);
                storage.add(a[5][2]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(10, 1, 2, 9, 10, 1));
    }

    @Test
    public void callOnceTest() {
        String code = """
                test.getArray()[test.getIndex()]++;
                storage.add(0);
                test.getArray()[test.getIndex()]--;
                storage.add(test.getArray()[2]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(0xABCD, 0xCDEF, 0, 0xABCD, 0xCDEF, 0xABCD, 30));
    }

    public static class ApiRoot {
        public static IntStorage storage;
        public static TestApi test = new TestApi();
    }

    public static class TestApi {

        private int[] array = new int[] { 10, 20, 30 };

        public int[] getArray() {
            ApiRoot.storage.add(0xABCD);
            return array;
        }

        public int getIndex() {
            ApiRoot.storage.add(0xCDEF);
            return 2;
        }
    }
}