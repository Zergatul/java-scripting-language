package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class TryStatementTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
    }

    @Test
    public void emptyTryCatchTest() {
        String code =
                "try {}\n" +
                "catch {}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryFinallyTest() {
        String code =
                "try {}\n" +
                "finally {}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryCatchFinallyTest() {
        String code =
                "try {}\n" +
                "catch {}\n" +
                "finally {}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void tryCatchNoExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4));
    }

    @Test
    public void tryCatchWithExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    int[] array = [];\n" +
                "    intStorage.add(2);\n" +
                "    array[1] = 10;\n" +
                "    intStorage.add(3);\n" +
                "} catch {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 5));
    }

    @Test
    public void tryCatchWithExceptionVariableTest() {
        String code =
                "try {\n" +
                "    int[] array = [];\n" +
                "    array[1] = 10;\n" +
                "} catch (e) {\n" +
                "    objectStorage.add(e);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());
        Assertions.assertTrue(ApiRoot.objectStorage.list.get(0) instanceof ArrayIndexOutOfBoundsException);
    }

    @Test
    public void tryFinallyNoExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4));
    }

    @Test
    public void tryFinallyWithExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "    int[] array = [];\n" +
                "    array[1] = 100;\n" +
                "    intStorage.add(3);\n" +
                "} finally {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        Runnable program = compile(ApiRoot.class, code);
        try {
            program.run();
        } catch (IndexOutOfBoundsException e) {
            ApiRoot.intStorage.add(10);
        }

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 10));
    }

    @Test
    public void tryFinallyInnerLoopBreakTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "    foreach (int x in [3, 4, 5]) {\n" +
                "        intStorage.add(x);\n" +
                "        if (x >= 4) {\n" +
                "            break;\n" +
                "        }\n" +
                "    }\n" +
                "    intStorage.add(6);\n" +
                "} finally {\n" +
                "    intStorage.add(7);\n" +
                "}\n" +
                "intStorage.add(8);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4, 6, 7, 8));
    }

    @Test
    public void tryFinallyInnerLoopContinueTest() {
        String code =
                "foreach (int x in [1, 2, 3]) {\n" +
                "    try {\n" +
                "        intStorage.add(x);\n" +
                "        if (x == 2) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        intStorage.add(100 + x);\n" +
                "    } finally {\n" +
                "        intStorage.add(1000 + x);\n" +
                "    }\n" +
                "    intStorage.add(2000 + x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                Lists.of(1, 101, 1001, 2001, 2, 1002, 3, 103, 1003, 2003),
                ApiRoot.intStorage.list);
    }

    @Test
    public void tryFinallyOuterLoopBreakTest() {
        String code =
                "intStorage.add(1);\n" +
                "foreach (int x in [2, 3, 4]) {\n" +
                "    try {\n" +
                "        intStorage.add(x);\n" +
                "        if (x >= 3) {\n" +
                "            break;\n" +
                "        }\n" +
                "        intStorage.add(-x);\n" +
                "    } finally {\n" +
                "        intStorage.add(5);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(6);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, -2, 5, 3, 5, 6));
    }

    @Test
    public void tryFinallyReturnTest() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        foreach (int x in [2, 3, 4]) {\n" +
                "            intStorage.add(x);\n" +
                "            if (x >= 3) {\n" +
                "                return x * x;\n" +
                "            }\n" +
                "            intStorage.add(-x);\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(10);\n" +
                "    }\n" +
                "    return 20;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, -2, 3, 10, 9));
    }

    @Test
    public void tryFinallyDoubleReturnTest() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        return 10;\n" +
                "    } finally {\n" +
                "        return 20;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(20));
    }

    @Test
    public void tryCatchFinallyNoExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(3);\n" +
                "} finally {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 5));
    }

    @Test
    public void tryCatchFinallyWithExceptionTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "    int[] array = [];\n" +
                "    array[1] = 100;\n" +
                "    intStorage.add(3);\n" +
                "} catch {\n" +
                "    intStorage.add(5);\n" +
                "} finally {\n" +
                "    intStorage.add(6);\n" +
                "}\n" +
                "intStorage.add(7);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 5, 6, 7));
    }

    @Test
    public void tryCatchFinallyWithExceptionVariableTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "    int[] array = [];\n" +
                "    array[1] = 100;\n" +
                "    intStorage.add(3);\n" +
                "} catch (e) {\n" +
                "    intStorage.add(5);\n" +
                "    objectStorage.add(e);\n" +
                "} finally {\n" +
                "    intStorage.add(6);\n" +
                "}\n" +
                "intStorage.add(7);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 5, 6, 7));
        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());
        Assertions.assertTrue(ApiRoot.objectStorage.list.get(0) instanceof ArrayIndexOutOfBoundsException);
    }

    @Test
    public void tryCatchFinallyReturnTest1() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    } finally {\n" +
                "        intStorage.add(10);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 1));
    }

    @Test
    public void tryCatchFinallyReturnTest2() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        int[] array = [];\n" +
                "        array[1] = 0;\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    } finally {\n" +
                "        intStorage.add(10);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 2));
    }

    @Test
    public void tryCatchFinallyDoubleReturnTest() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    } finally {\n" +
                "        return 3;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3));
    }

    @Test
    public void tryCatchControlFlowTest() {
        String code =
                "void noop() {}\n" +
                "int func1() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func2() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        return 1;\n" +
                "    }\n" +
                "}\n" +
                "int func3() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func4() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    }\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(8, 1, 93, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(15, 1, 173, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(22, 1, 251, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void tryFinallyControlFlowTest() {
        String code =
                "void noop() {}\n" +
                "int func1() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func2() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        return 1;\n" +
                "    }\n" +
                "}\n" +
                "int func3() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func4() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } finally {\n" +
                "        return 2;\n" +
                "    }\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(22, 1, 257, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void tryCatchFinallyControlFlowTest() {
        String code =
                "void noop() {}\n" +
                "int func1() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func2() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func3() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        return 1;\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func4() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        return 1;\n" +
                "    }\n" +
                "}\n" +
                "int func5() {\n" +
                "    try {\n" +
                "        noop();\n" +
                "    } catch {\n" +
                "        return 1;\n" +
                "    } finally {\n" +
                "        return 2;\n" +
                "    }\n" +
                "}\n" +
                "int func6() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        noop();\n" +
                "    } finally {\n" +
                "        return 2;\n" +
                "    }\n" +
                "}\n" +
                "int func7() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    } finally {\n" +
                "        noop();\n" +
                "    }\n" +
                "}\n" +
                "int func8() {\n" +
                "    try {\n" +
                "        return 1;\n" +
                "    } catch {\n" +
                "        return 2;\n" +
                "    } finally {\n" +
                "        return 3;\n" +
                "    }\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(10, 1, 123, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(19, 1, 235, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(28, 1, 347, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void rethrowTest() {
        String code =
                "try {\n" +
                "    int[] array = [];\n" +
                "    array[1] = 0;\n" +
                "} catch {\n" +
                "    intStorage.add(10);\n" +
                "    throw;\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(IndexOutOfBoundsException.class, program::run);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
    }

    @Test
    public void rethrowWithFinallyTest() {
        String code =
                "try {\n" +
                "    int[] array = [];\n" +
                "    array[1] = 0;\n" +
                "} catch {\n" +
                "    intStorage.add(4);\n" +
                "    throw;\n" +
                "} finally {\n" +
                "    intStorage.add(5);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(IndexOutOfBoundsException.class, program::run);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(4, 5));
    }

    @Test
    public void throwFromCatchWithFinallyTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "try {\n" +
                "    int[] array = [];\n" +
                "    array[1] = 0;\n" +
                "} catch {\n" +
                "    intStorage.add(4);\n" +
                "    throw new RuntimeException();\n" +
                "} finally {\n" +
                "    intStorage.add(5);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(4, 5));
    }

    @Test
    public void throwExceptionVariableWithFinallyTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "try {\n" +
                "    int[] array = [];\n" +
                "    array[1] = 0;\n" +
                "} catch (e) {\n" +
                "    intStorage.add(4);\n" +
                "    throw e;\n" +
                "} finally {\n" +
                "    intStorage.add(5);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(IndexOutOfBoundsException.class, program::run);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(4, 5));
    }

    @Test
    public void throwFromFinallyOverridesExceptionFromTryTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    [1][2] = 0; // throws\n" +
                "} finally {\n" +
                "    intStorage.add(2);\n" +
                "    throw new RuntimeException(\"finally\");\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, program::run);
        Assertions.assertEquals("finally", ex.getMessage());
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void returnInFinallySuppressesExceptionTest() {
        String code =
                "int func() {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        [1][2] = 0; // throws\n" +
                "        return 111;\n" +
                "    } finally {\n" +
                "        intStorage.add(2);\n" +
                "        return 123;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 123), ApiRoot.intStorage.list);
    }

    @Test
    public void finallyOverridesCatchThrowTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "try {\n" +
                "    [1][2] = 0; // throws\n" +
                "} catch {\n" +
                "    intStorage.add(1);\n" +
                "    throw new RuntimeException(\"catch\");\n" +
                "} finally {\n" +
                "    intStorage.add(2);\n" +
                "    throw new RuntimeException(\"finally\");\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, program::run);

        Assertions.assertEquals("finally", ex.getMessage());
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void exceptionInCatchShouldRunFinally() {
        String code =
                "try {\n" +
                "    [1][2] = 0; // throws\n" +
                "} catch {\n" +
                "    intStorage.add(1);\n" +
                "    [1][2] = 0; // throws\n" +
                "} finally {\n" +
                "    intStorage.add(2);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(IndexOutOfBoundsException.class, program::run);

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedTryFinallyTest() {
        String code =
                "try {\n" +
                "     try {\n" +
                "         intStorage.add(1);\n" +
                "         [1][2] = 3; // throws\n" +
                "         intStorage.add(999);\n" +
                "     } finally {\n" +
                "         intStorage.add(2);\n" +
                "     }\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(IndexOutOfBoundsException.class, program::run);

        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }
}