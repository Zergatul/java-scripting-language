package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.utility.MarkedDiagnostic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class NullTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void cannotAssignNullToValueTypesTest() {
        String code =
                "boolean b = ⟦null⟧;\n" +
                "int i = ⟪null⟫;\n" +
                "char c = ⸨null⸩;\n" +
                "float f = ⟬null⟭;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code,
                new MarkedDiagnostic(
                        "⟦⟧",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "boolean"),
                new MarkedDiagnostic(
                        "⟪⟫",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "int"),
                new MarkedDiagnostic(
                        "⸨⸩",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "char"),
                new MarkedDiagnostic(
                        "⟬⟭",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "float"));
    }

    @Test
    public void cannotReturnNullFromValueFunctionTest() {
        String code =
                "int f() => ⟦null⟧;\n" +
                "boolean g() {\n" +
                "    return ⟪null⟫;\n" +
                "}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code,
                new MarkedDiagnostic(
                        "⟦⟧",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "int"),
                new MarkedDiagnostic(
                        "⟪⟫",
                        BinderErrors.CannotImplicitlyConvert,
                        "null", "boolean"));
    }

    @Test
    public void cannotPassNullToValueParameterTest() {
        String code =
                "void takesInt(int x) {}\n" +
                "void takesBool(boolean b) {}\n" +
                "\n" +
                "takesInt⟦(null)⟧;\n" +
                "takesBool⟪(null)⟫;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code,
                new MarkedDiagnostic(
                        "⟦⟧",
                        BinderErrors.FunctionInvalidArguments,
                        "takesInt",
                        "Candidates:\nvoid takesInt(int x)"),
                new MarkedDiagnostic(
                        "⟪⟫",
                        BinderErrors.FunctionInvalidArguments,
                        "takesBool",
                        "Candidates:\nvoid takesBool(boolean b)"));
    }

    @Test
    public void canPassNullToReferenceParameterTest() {
        String code =
                "void takesString(string s) => stringStorage.add(s);\n" +
                "void takesObject(Java<java.lang.Object> o) {\n" +
                "    boolStorage.add(o == null);\n" +
                "}\n" +
                "\n" +
                "takesString(null);\n" +
                "takesObject(null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true));
    }

    @Test
    public void stringAsNullTest() {
        String code =
                "string s = null;\n" +
                "stringStorage.add(s);\n" +
                "stringStorage.add(null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
    }

    @Test
    public void conditionalExpressionTest() {
        String code =
                "boolean b = true;\n" +
                "string s = b ? null : \"123\";\n" +
                "stringStorage.add(s);\n" +
                "b = false;\n" +
                "stringStorage.add(b ? null : \"456\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("456");
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
    }

    @Test
    public void conditionalExpressionWithDifferentReferenceTypesTest() {
        String code =
                "typealias JObject = Java<java.lang.Object>;\n" +
                "typealias JString = Java<java.lang.String>;\n" +
                "\n" +
                "JObject o = new JString(\"hi\");\n" +
                "boolean cond = true;\n" +
                "\n" +
                "// both sides null\n" +
                "string s1 = cond ? null : null;\n" +
                "\n" +
                "// null vs string\n" +
                "string s2 = cond ? \"a\" : null;\n" +
                "string s3 = cond ? null : \"b\";\n" +
                "\n" +
                "// null vs Java<String>\n" +
                "JString js = cond ? null : new JString(\"x\");\n" +
                "\n" +
                "stringStorage.add(s1);\n" +
                "stringStorage.add(s2);\n" +
                "stringStorage.add(s3);\n" +
                "stringStorage.add(js);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("a");
        list.add(null);
        list.add(null);

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                list);
    }

    @Test
    public void conditionalExpressionIncompatibleTypesWithNullTest() {
        String code =
                "boolean b = true;\n" +
                "let x = ⟦b ? null : 42⟧;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.CannotDetermineConditionalExpressionType,
                "null", "int");
    }

    @Test
    public void nullCheckTest() {
        String code =
                "string func1() => null;\n" +
                "string func2() => \"00\";\n" +
                "\n" +
                "boolStorage.add(func1() == null);\n" +
                "boolStorage.add(func1() != null);\n" +
                "boolStorage.add(func2() == null);\n" +
                "boolStorage.add(func2() != null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false, false, true));
    }

    @Test
    public void nullEqualsNullTest() {
        String code =
                "boolStorage.add(null == null);\n" +
                "boolStorage.add(null != null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void nullEqualityVsValueTypesTest() {
        String code =
                "boolStorage.add(null == 0);\n" +
                "boolStorage.add(0 == null);\n" +
                "boolStorage.add(null != 0);\n" +
                "boolStorage.add(0 != null);\n" +
                "\n" +
                "boolStorage.add(null == 0.0);\n" +
                "boolStorage.add(0.0 == null);\n" +
                "boolStorage.add(null != 0.0);\n" +
                "boolStorage.add(0.0 != null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        false, false, true, true,
                        false, false, true, true));
    }

    @Test
    public void nullEqualityWithReferencesTest() {
        String code =
                "string a = null;\n" +
                "string b = null;\n" +
                "string c = \"x\";\n" +
                "\n" +
                "boolStorage.add(a == null);\n" +
                "boolStorage.add(null == b);\n" +
                "boolStorage.add(a == b);\n" +
                "boolStorage.add(a == c);\n" +
                "boolStorage.add(c != null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, true, false, true));
    }

    @Test
    public void isOperatorWithNullAndAliasesTest() {
        String code =
                "typealias JString = Java<java.lang.String>;\n" +
                "\n" +
                "Java<java.lang.Object> a = null;\n" +
                "Java<java.lang.Object> b = new JString(\"hi\");\n" +
                "\n" +
                "boolStorage.add(a is string);   // null\n" +
                "boolStorage.add(a is JString);  // null\n" +
                "boolStorage.add(b is string);   // underlying java.lang.String\n" +
                "boolStorage.add(b is JString);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(false, false, true, true));
    }

    @Test
    public void asOperatorWithNullAndAliasesTest() {
        String code =
                "typealias JString = Java<java.lang.String>;\n" +
                "\n" +
                "Java<java.lang.Object> a = null;\n" +
                "Java<java.lang.Object> b = new JString(\"hi\");\n" +
                "\n" +
                "string s1 = a as string;\n" +
                "JString s2 = a as JString;\n" +
                "string s3 = b as string;\n" +
                "JString s4 = b as JString;\n" +
                "\n" +
                "stringStorage.add(s1);\n" +
                "stringStorage.add(s2);\n" +
                "stringStorage.add(s3);\n" +
                "stringStorage.add(s4);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Arrays.asList(null, null, "hi", "hi"));
    }

    @Test
    public void indexerOnNullThrowsTest() {
        String code =
                "string s = null;\n" +
                "char c = s[0];\n";

        Runnable program = compile(ApiRoot.class, code);

        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void lengthOnNullThrowsTest() {
        String code =
                "string s = null;\n" +
                "int len = s.length;\n";

        Runnable program = compile(ApiRoot.class, code);

        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void arraysWithNullsTest() {
        String code =
                "string[] arr = new string[3];\n" +
                "arr[0] = null;\n" +
                "arr[1] = \"x\";\n" +
                "arr[2] = null;\n" +
                "\n" +
                "for (int i = 0; i < 3; i++) {\n" +
                "    stringStorage.add(arr[i]);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Arrays.asList(null, "x", null));
    }

    @Test
    public void letNullTest() {
        String code =
                "⟦let⟧ x = null;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.LetNull);
    }

    @Test
    public void nullMembersTest() {
        String code =
                "null⟦.⟧abc();\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.CannotAccessNullMembers);
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}