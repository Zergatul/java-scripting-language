package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Float32Storage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class Float32Tests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.float32Storage = new Float32Storage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "float32 f;\n" +
                "float32Storage.add(f);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.float32Storage.list,
                Lists.of(0.0f));
    }

    @Test
    public void implicitCastTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "float f = parse(\"1.5\");\n" +
                "floatStorage.add(f);\n" +
                "\n" +
                "float32Storage.add(4000000000L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(1.5));
        Assertions.assertIterableEquals(ApiRoot.float32Storage.list, Lists.of(4e9f));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "float32 f = parse(\"1.5\") + parse(\"5.5\");\n" +
                "float32Storage.add(f);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.float32Storage.list,
                Lists.of(7.0f));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"0.5\") == parse(\"0.5\"));\n" +
                "boolStorage.add(parse(\"1.5\") + parse(\"1.5\") == parse(\"0.5\") + parse(\"2.5\"));\n" +
                "boolStorage.add(parse(\"0.5\") == parse(\"0.6\"));\n" +
                "boolStorage.add(parse(\"1.5\") + parse(\"1.6\") == parse(\"0.5\") + parse(\"2.5\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"0.5\") != parse(\"0.5\"));\n" +
                "boolStorage.add(parse(\"1.5\") + parse(\"1.5\") != parse(\"0.5\") + parse(\"2.5\"));\n" +
                "boolStorage.add(parse(\"0.5\") != parse(\"0.6\"));\n" +
                "boolStorage.add(parse(\"1.5\") + parse(\"1.6\") != parse(\"0.5\") + parse(\"2.5\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(false, false, true, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"10000.0\") < parse(\"10001.0\"));\n" +
                "boolStorage.add(parse(\"10001.0\") < parse(\"10000.0\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"123456.0\") > parse(\"-1235456.0\"));\n" +
                "boolStorage.add(parse(\"-1235456.0\") > parse(\"123456.0\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"1000000.0\") <= parse(\"1000000.0\"));\n" +
                "boolStorage.add(parse(\"1000000.0\") <= parse(\"1000001.0\"));\n" +
                "boolStorage.add(parse(\"1000000.0\") <= parse(\"999999.0\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(parse(\"1000000.0\") >= parse(\"1000000.0\"));\n" +
                "boolStorage.add(parse(\"1000001.0\") >= parse(\"1000000.0\"));\n" +
                "boolStorage.add(parse(\"999999.0\") >= parse(\"1000000.0\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    @Test
    public void minusTest() {
        String code =
                "float32 f = -123;\n" +
                "float32Storage.add(- -f);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.float32Storage.list,
                Lists.of(-123.0f));
    }

    @Test
    public void implicitCastTest1() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "float32Storage.add(parse(\"1.5\") + 1);\n" +
                "float32Storage.add(1 + parse(\"1.5\"));\n" +
                "float32Storage.add(parse(\"1.5\") - 1);\n" +
                "float32Storage.add(2 - parse(\"1.5\"));\n" +
                "float32Storage.add(parse(\"1.5\") * 2);\n" +
                "float32Storage.add(parse(\"1.2\") / 2);\n" +
                "float32Storage.add(1);\n" +
                "\n" +
                "boolStorage.add(parse(\"1.5\") > 1);\n" +
                "boolStorage.add(parse(\"1.5\") < 1);\n" +
                "boolStorage.add(parse(\"1.5\") >= 1);\n" +
                "boolStorage.add(parse(\"1.5\") <= 1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.float32Storage.list,
                Lists.of(2.5f, 2.5f, 0.5f, 0.5f, 3.0f, 0.6f, 1.0f));
        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false, true, false));
    }

    @Test
    public void implicitCastTest2() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "boolStorage.add(1 + parse(\"0.5\") == parse(\"1.5\"));\n" +
                "boolStorage.add(parse(\"0.5\") + 1 == parse(\"1.5\"));\n" +
                "boolStorage.add(parse(\"1.5\") - parse(\"0.5\") == parse(\"1.0\"));\n" +
                "boolStorage.add(1 - parse(\"0.5\") == parse(\"0.5\"));\n" +
                "\n" +
                "boolStorage.add(parse(\"1.5\") * 2 == parse(\"3.0\"));\n" +
                "boolStorage.add(2 * parse(\"1.5\") == parse(\"3.0\"));\n" +
                "boolStorage.add(parse(\"3.0\") / 2 == parse(\"1.5\"));\n" +
                "boolStorage.add(3 / parse(\"2.0\") == parse(\"1.5\"));\n" +
                "boolStorage.add(parse(\"3.0\") % 2 == parse(\"1.0\"));\n" +
                "boolStorage.add(3 % parse(\"2.0\") == parse(\"1.0\"));\n" +
                "\n" +
                "boolStorage.add(parse(\"1.9\") < 2);\n" +
                "boolStorage.add(2 < parse(\"2.1\"));\n" +
                "boolStorage.add(parse(\"2.1\") > 2);\n" +
                "boolStorage.add(2 > parse(\"1.9\"));\n" +
                "\n" +
                "boolStorage.add(parse(\"1.9\") <= 2);\n" +
                "boolStorage.add(parse(\"2.0\") <= 2);\n" +
                "boolStorage.add(2 <= parse(\"2.1\"));\n" +
                "boolStorage.add(2 <= parse(\"2.0\"));\n" +
                "boolStorage.add(parse(\"2.1\") >= 2);\n" +
                "boolStorage.add(parse(\"2.0\") >= 2);\n" +
                "boolStorage.add(2 >= parse(\"1.9\"));\n" +
                "boolStorage.add(2 >= parse(\"2.0\"));\n" +
                "\n" +
                "boolStorage.add(parse(\"2.0\") == 2);\n" +
                "boolStorage.add(2 == parse(\"2.0\"));\n" +
                "boolStorage.add(parse(\"2.1\") != 2);\n" +
                "boolStorage.add(2 != parse(\"2.1\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(ApiRoot.boolStorage.list.size(), 26);
        for (int i = 0; i < 26; i++) {
            Assertions.assertTrue(ApiRoot.boolStorage.list.get(i));
        }
    }

    @Test
    public void toStringTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "float32 x = parse(\"100.25\");\n" +
                "stringStorage.add(x.toString());\n" +
                "stringStorage.add(parse(\"10.125\").toString());\n" +
                "\n" +
                "stringStorage.add(parse(\"10000.5555\").toStandardString(3));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("100.25", "10.125", "10,000.556"));
    }

    @Test
    public void tryParseTest() {
        String code =
                "float32 x;\n" +
                "boolStorage.add(float32.tryParse(\"0.75\", ref x));\n" +
                "float32Storage.add(x);\n" +
                "\n" +
                "boolStorage.add(float32.tryParse(\"0.0.0\", ref x));\n" +
                "float32Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.float32Storage.list,
                Lists.of(0.75f, 0.75f));
    }

    @Test
    public void leftAssociativityTest() {
        String code =
                "float32 parse(string s) {\n" +
                "    float32 f;\n" +
                "    float32.tryParse(s, ref f);\n" +
                "    return f;\n" +
                "}\n" +
                "\n" +
                "int x1 = 25;\n" +
                "int x2 = 200;\n" +
                "float32 percents = parse(\"100.0\") * x1 / x2;\n" +
                "float32Storage.add(percents);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.float32Storage.list, Lists.of(12.5f));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Float32Storage float32Storage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}