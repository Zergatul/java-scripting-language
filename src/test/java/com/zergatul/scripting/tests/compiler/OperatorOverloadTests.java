package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.BinaryOperatorMethod;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.Setter;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomType;

public class OperatorOverloadTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void customTypeBinaryOverloadTest() {
        String code = """
                let v1 = new Vector3f(1, 2, 3);
                let v2 = new Vector3f(2, 0, 0);
                stringStorage.add((v1 + v2).toString());
                stringStorage.add((v1 - v2).toString());
                stringStorage.add((v1 * v2).toString());
                stringStorage.add((5.0 * v1).toString());
                stringStorage.add((v2 * 3.0).toString());
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, Vector3f.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of(
                        "(3.0, 2.0, 3.0)",
                        "(-1.0, 2.0, 3.0)",
                        "2.0",
                        "(5.0, 10.0, 15.0)",
                        "(6.0, 0.0, 0.0)"));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
    @CustomType(name = "Vector3f")
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

        @Override
        public String toString() {
            return String.format("(%s, %s, %s)", x, y, z);
        }

        @BinaryOperatorMethod(BinaryOperator.PLUS)
        public static Vector3f op_Add(Vector3f v1, Vector3f v2) {
            return new Vector3f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
        }

        @BinaryOperatorMethod(BinaryOperator.MINUS)
        public static Vector3f op_Subtract(Vector3f v1, Vector3f v2) {
            return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
        }

        @BinaryOperatorMethod(BinaryOperator.MULTIPLY)
        public static double op_Multiply(Vector3f v1, Vector3f v2) {
            return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        }

        @BinaryOperatorMethod(BinaryOperator.MULTIPLY)
        public static Vector3f op_Multiply(Vector3f v, double factor) {
            return new Vector3f(factor * v.x, factor * v.y, factor * v.z);
        }

        @BinaryOperatorMethod(BinaryOperator.MULTIPLY)
        public static Vector3f op_Multiply(double factor, Vector3f v) {
            return new Vector3f(factor * v.x, factor * v.y, factor * v.z);
        }
    }
}