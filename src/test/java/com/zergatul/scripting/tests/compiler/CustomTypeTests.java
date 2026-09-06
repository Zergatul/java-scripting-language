package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class CustomTypeTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void simpleTest() {
        String code =
                "vector3f vector = api.getVector(1, 2, 2);\n" +
                "floatStorage.add(vector.x);\n" +
                "floatStorage.add(vector.y);\n" +
                "floatStorage.add(vector.z);\n" +
                "floatStorage.add(vector.length);\n" +
                "vector.y = 4;\n" +
                "vector.z = 8;\n" +
                "floatStorage.add(vector.x);\n" +
                "floatStorage.add(vector.y);\n" +
                "floatStorage.add(vector.z);\n" +
                "floatStorage.add(vector.length);\n" +
                "vector.x += 1;\n" +
                "vector.y += 2;\n" +
                "vector.z += 1;\n" +
                "floatStorage.add(vector.x);\n" +
                "floatStorage.add(vector.y);\n" +
                "floatStorage.add(vector.z);\n" +
                "floatStorage.add(vector.length);\n";

        Runnable program = compileWithCustomType(ApiRoot.class, Vector3f.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                Lists.of(
                        1.0, 2.0, 2.0, 3.0,
                        1.0, 4.0, 8.0, 9.0,
                        2.0, 6.0, 9.0, 11.0));
    }

    @Test
    public void indexGetterTest() {
        String code =
                "let tester = api.getIndexTester();\n" +
                "intStorage.add(tester[\"a\"]);\n" +
                "intStorage.add(tester[\"ab\"]);\n" +
                "intStorage.add(tester[\"abcd\"]);\n" +
                "stringStorage.add(tester[123]);\n" +
                "stringStorage.add(tester[10.0]);\n" +
                "stringStorage.add(tester[[1, 2, 3, 4, 5, 6]]);\n";

        Runnable program = compileWithCustomType(ApiRoot.class, IndexTester.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("15129", "99.75", "21"));
    }

    @Test
    public void indexSetterTest() {
        String code =
                "let collection = new NameValueCollection();\n" +
                "collection[\"a\"] = \"apple\";\n" +
                "collection[\"b\"] = \"banana\";\n" +
                "collection[\"c\"] = \"coconut\";\n" +
                "stringStorage.add(collection[\"b\"]);\n" +
                "stringStorage.add(collection[\"a\"]);\n" +
                "stringStorage.add(collection[\"c\"]);\n";

        Runnable program = compileWithCustomType(ApiRoot.class, NameValueCollection.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("banana", "apple", "coconut"));
    }

    @Test
    public void cannotInstantiateAbstractClassTest() {
        String code =
                "let instance = new AbstractClass();\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.CannotInstantiateAbstractClass,
                                new SingleLineTextRange(1, 16, 15, 19))),
                getDiagnostics(ApiRoot.class, code, AbstractClass.class));
    }

    @Test
    public void staticMembersTest() {
        String code =
                "intStorage.add(ChildClass.FIELD2);\n" +
                "intStorage.add(ChildClass.method2());\n";

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, BaseClass.class, ChildClass.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(456, 567));
    }

    @Test
    public void cannotUseBaseStaticMembersTest() {
        String code =
                "intStorage.add(ChildClass.FIELD1);\n" +
                "intStorage.add(ChildClass.method1());\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.MemberDoesNotExist,
                                new SingleLineTextRange(1, 27, 26, 6),
                                "ChildClass", "FIELD1"),
                        new DiagnosticMessage(
                                BinderErrors.MemberDoesNotExist,
                                new SingleLineTextRange(2, 27, 61, 7),
                                "ChildClass", "method1")),
                getDiagnostics(ApiRoot.class, code, BaseClass.class, ChildClass.class));
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

    @SuppressWarnings("unused")
    @CustomType(name = "NameValueCollection")
    public static class NameValueCollection {

        private final Map<String, String> map = new HashMap<>();

        @IndexGetter
        public String getByIndex(String index) {
            return map.get(index);
        }

        @IndexSetter
        public void setByIndex(String index, String value) {
            map.put(index, value);
        }
    }

    @CustomType(name = "AbstractClass")
    public static abstract class AbstractClass {}

    @CustomType(name = "BaseClass")
    public static class BaseClass {

        public static final int FIELD1 = 234;

        public static int method1() {
            return 345;
        }
    }

    @CustomType(name = "ChildClass")
    public static class ChildClass extends BaseClass {

        public static final int FIELD2 = 456;

        public static int method2() {
            return 567;
        }
    }
}