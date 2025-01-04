package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.Setter;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomType;

public class CustomTypeTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void simpleTest() {
        String code = """
                vector3f vector = api.getVector(1, 2, 2);
                floatStorage.add(vector.x);
                floatStorage.add(vector.y);
                floatStorage.add(vector.z);
                floatStorage.add(vector.length);
                vector.y = 4;
                vector.z = 8;
                floatStorage.add(vector.x);
                floatStorage.add(vector.y);
                floatStorage.add(vector.z);
                floatStorage.add(vector.length);
                vector.x += 1;
                vector.y += 2;
                vector.z += 1;
                floatStorage.add(vector.x);
                floatStorage.add(vector.y);
                floatStorage.add(vector.z);
                floatStorage.add(vector.length);
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, Vector3f.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(
                        1.0, 2.0, 2.0, 3.0,
                        1.0, 4.0, 8.0, 9.0,
                        2.0, 6.0, 9.0, 11.0));
    }

    @Test
    public void indexersTest() {
        String code = """
                let tester = api.getIndexTester();
                intStorage.add(tester["a"]);
                intStorage.add(tester["ab"]);
                intStorage.add(tester["abcd"]);
                stringStorage.add(tester[123]);
                stringStorage.add(tester[10.0]);
                stringStorage.add(tester[[1, 2, 3, 4, 5, 6]]);
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, IndexTester.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("15129", "99.75", "21"));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {

        public Vector3f getVector(double x, double y, double z) {
            return new Vector3f(x, y, z);
        }

        public IndexTester getIndexTester() {
            return new IndexTester();
        }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "vector3f")
    public static class Vector3f {

        private double x;
        private double y;
        private double z;

        public Vector3f(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Getter(name = "x")
        public double getX() {
            return x;
        }

        @Getter(name = "y")
        public double getY() {
            return y;
        }

        @Getter(name = "z")
        public double getZ() {
            return z;
        }

        @Setter(name = "x")
        public void setX(double value) {
            x = value;
        }

        @Setter(name = "y")
        public void setY(double value) {
            y = value;
        }

        @Setter(name = "z")
        public void setZ(double value) {
            z = value;
        }

        @Getter(name = "length")
        public double getLength() {
            return Math.sqrt(x * x + y * y + z * z);
        }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "IndexTester")
    public static class IndexTester {

        @IndexGetter
        public int stringIndexer(String value) {
            return value.length();
        }

        @IndexGetter
        public String intIndexer(int value) {
            return Integer.toString(value * value);
        }

        @IndexGetter
        public String floatIndexer(double value) {
            return Double.toString((value -0.5) * (value + 0.5));
        }

        @IndexGetter
        public String arrayIndexer(int[] value) {
            return Integer.toString(Arrays.stream(value).sum());
        }
    }
}