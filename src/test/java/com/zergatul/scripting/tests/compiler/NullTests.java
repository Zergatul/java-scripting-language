package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

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
        String code = """
                boolean b = null;
                int i = null;
                char c = null;
                float f = null;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(1, 13, 12, 4), "null", "boolean"),
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(2, 9, 26, 4), "null", "int"),
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(3, 10, 41, 4), "null", "char"),
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(4, 11, 57, 4), "null", "float")),
                getDiagnostics(LetTests.ApiRoot.class, code));
    }

    @Test
    public void cannotReturnNullFromValueFunctionTest() {
        String code = """
                int f() => null;
                boolean g() {
                    return null;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(1, 12, 11, 4), "null", "int"),
                        new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(3, 12, 42, 4), "null", "boolean")),
                getDiagnostics(LetTests.ApiRoot.class, code));
    }

    @Test
    public void cannotPassNullToValueParameterTest() {
        String code = """
                void takesInt(int x) {}
                void takesBool(boolean b) {}
                
                takesInt(null);
                takesBool(null);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotCastArguments, new SingleLineTextRange(4, 9, 62, 6), "null", "int"),
                        new DiagnosticMessage(BinderErrors.CannotCastArguments, new SingleLineTextRange(5, 10, 79, 6), "null", "boolean")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void canPassNullToReferenceParameterTest() {
        String code = """
                void takesString(string s) => stringStorage.add(s);
                void takesObject(Java<java.lang.Object> o) {
                    boolStorage.add(o == null);
                }
                
                takesString(null);
                takesObject(null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true));
    }

    @Test
    public void stringAsNullTest() {
        String code = """
                string s = null;
                stringStorage.add(s);
                stringStorage.add(null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
    }

    @Test
    public void conditionalExpressionTest() {
        String code = """
                boolean b = true;
                string s = b ? null : "123";
                stringStorage.add(s);
                b = false;
                stringStorage.add(b ? null : "456");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("456");
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, list);
    }

    @Test
    public void conditionalExpressionWithDifferentReferenceTypesTest() {
        String code = """
                typealias JObject = Java<java.lang.Object>;
                typealias JString = Java<java.lang.String>;
                
                JObject o = new JString("hi");
                boolean cond = true;
                
                // both sides null
                string s1 = cond ? null : null;
                
                // null vs string
                string s2 = cond ? "a" : null;
                string s3 = cond ? null : "b";
                
                // null vs Java<String>
                JString js = cond ? null : new JString("x");
                
                stringStorage.add(s1);
                stringStorage.add(s2);
                stringStorage.add(s3);
                stringStorage.add(js);
                """;

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
        String code = """
                boolean b = true;
                let x = b ? null : 42;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotDetermineConditionalExpressionType, new SingleLineTextRange(2,9,26,13), "null", "int")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void nullCheckTest() {
        String code = """
                string func1() => null;
                string func2() => "00";
                
                boolStorage.add(func1() == null);
                boolStorage.add(func1() != null);
                boolStorage.add(func2() == null);
                boolStorage.add(func2() != null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false, true));
    }

    @Test
    public void nullEqualityVsValueTypesTest() {
        String code = """
                int i = 0;
                boolean b1 = null == i;
                boolean b2 = i != null;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BinaryOperatorNotDefined, new SingleLineTextRange(2, 14, 24, 9), "==", "null", "int"),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorNotDefined, new SingleLineTextRange(3, 14, 48, 9), "!=", "int", "null")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void nullEqualityWithReferencesTest() {
        String code = """
                string a = null;
                string b = null;
                string c = "x";
                
                boolStorage.add(a == null);
                boolStorage.add(null == b);
                boolStorage.add(a == b);
                boolStorage.add(a == c);
                boolStorage.add(c != null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, true, false, true));
    }

    @Test
    public void isOperatorWithNullAndAliasesTest() {
        String code = """
                typealias JString = Java<java.lang.String>;
                
                Java<java.lang.Object> a = null;
                Java<java.lang.Object> b = new JString("hi");
                
                boolStorage.add(a is string);   // null
                boolStorage.add(a is JString);  // null
                boolStorage.add(b is string);   // underlying java.lang.String
                boolStorage.add(b is JString);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, false, true, true));
    }

    @Test
    public void asOperatorWithNullAndAliasesTest() {
        String code = """
                typealias JString = Java<java.lang.String>;
                
                Java<java.lang.Object> a = null;
                Java<java.lang.Object> b = new JString("hi");
                
                string s1 = a as string;
                JString s2 = a as JString;
                string s3 = b as string;
                JString s4 = b as JString;
                
                stringStorage.add(s1);
                stringStorage.add(s2);
                stringStorage.add(s3);
                stringStorage.add(s4);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Arrays.asList(null, null, "hi", "hi"));
    }

    @Test
    public void indexerOnNullThrowsTest() {
        String code = """
                string s = null;
                char c = s[0];
                """;

        Runnable program = compile(ApiRoot.class, code);

        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void lengthOnNullThrowsTest() {
        String code = """
                string s = null;
                int len = s.length;
                """;

        Runnable program = compile(ApiRoot.class, code);

        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void arraysWithNullsTest() {
        String code = """
                string[] arr = new string[3];
                arr[0] = null;
                arr[1] = "x";
                arr[2] = null;
                
                for (int i = 0; i < 3; i++) {
                    stringStorage.add(arr[i]);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Arrays.asList(null, "x", null));
    }

    @Test
    public void letNullTest() {
        String code = """
                let x = null;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.LetNull, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(LetTests.ApiRoot.class, code));
    }

    @Test
    public void nullMembersTest() {
        String code = """
                null.abc();
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotAccessNullMembers, new SingleLineTextRange(1, 5, 4, 1))),
                getDiagnostics(LetTests.ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}