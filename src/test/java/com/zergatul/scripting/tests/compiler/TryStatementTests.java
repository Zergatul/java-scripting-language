package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
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
    }

    @Test
    public void emptyTryCatchTest() {
        String code = """
                try {}
                catch {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryFinallyTest() {
        String code = """
                try {}
                finally {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryCatchFinallyTest() {
        String code = """
                try {}
                catch {}
                finally {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void tryCatchNoExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4));
    }

    @Test
    public void tryCatchWithExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    int[] array = [];
                    intStorage.add(2);
                    array[1] = 10;
                    intStorage.add(3);
                } catch {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
    }

    @Test
    public void tryCatchWithExceptionVariableTest() {
        String code = """
                try {
                    int[] array = [];
                    array[1] = 10;
                } catch (e) {
                    stringStorage.add(e.getMessage());
                    stringStorage.add(e.toString());
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(
                "Index 1 out of bounds for length 0",
                "java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0"));
    }

    @Test
    public void tryFinallyNoExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                } finally {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4));
    }

    @Test
    public void tryFinallyWithExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                    int[] array = [];
                    array[1] = 100;
                    intStorage.add(3);
                } finally {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        try {
            program.run();
        } catch (IndexOutOfBoundsException e) {
            ApiRoot.intStorage.add(10);
        }

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 10));
    }

    @Test
    public void tryFinallyInnerLoopBreakTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                    foreach (int x in [3, 4, 5]) {
                        intStorage.add(x);
                        if (x >= 4) {
                            break;
                        }
                    }
                    intStorage.add(6);
                } finally {
                    intStorage.add(7);
                }
                intStorage.add(8);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 6, 7, 8));
    }

    @Test
    public void tryFinallyOuterLoopBreakTest() {
        String code = """
                intStorage.add(1);
                foreach (int x in [2, 3, 4]) {
                    try {
                        intStorage.add(x);
                        if (x >= 3) {
                            break;
                        }
                        intStorage.add(-x);
                    } finally {
                        intStorage.add(5);
                    }
                }
                intStorage.add(6);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, -2, 5, 3, 5, 6));
    }

    @Test
    public void tryFinallyReturnTest() {
        String code = """
                int func() {
                    try {
                        foreach (int x in [2, 3, 4]) {
                            intStorage.add(x);
                            if (x >= 3) {
                                return x * x;
                            }
                            intStorage.add(-x);
                        }
                    } finally {
                        intStorage.add(10);
                    }
                    return 20;
                }
                
                intStorage.add(func());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, -2, 3, 10, 9));
    }

    @Test
    public void tryFinallyDoubleReturnTest() {
        String code = """
                int func() {
                    try {
                        return 10;
                    } finally {
                        return 20;
                    }
                }
                
                intStorage.add(func());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20));
    }

    @Test
    public void tryCatchFinallyNoExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                } finally {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
    }

    @Test
    public void tryCatchFinallyWithExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                    int[] array = [];
                    array[1] = 100;
                    intStorage.add(3);
                } catch {
                    intStorage.add(5);
                } finally {
                    intStorage.add(6);
                }
                intStorage.add(7);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 5, 6, 7));
    }

    @Test
    public void tryCatchFinallyWithExceptionVariableTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                    int[] array = [];
                    array[1] = 100;
                    intStorage.add(3);
                } catch (e) {
                    intStorage.add(5);
                    stringStorage.add(e.getMessage());
                } finally {
                    intStorage.add(6);
                }
                intStorage.add(7);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 5, 6, 7));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Index 1 out of bounds for length 0"));
    }

    @Test
    public void tryCatchFinallyReturnTest1() {
        String code = """
                int func() {
                    try {
                        return 1;
                    } catch {
                        return 2;
                    } finally {
                        intStorage.add(10);
                    }
                }
                
                intStorage.add(func());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 1));
    }

    @Test
    public void tryCatchFinallyReturnTest2() {
        String code = """
                int func() {
                    try {
                        int[] array = [];
                        array[1] = 0;
                        return 1;
                    } catch {
                        return 2;
                    } finally {
                        intStorage.add(10);
                    }
                }
                
                intStorage.add(func());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 2));
    }

    @Test
    public void tryCatchFinallyDoubleReturnTest() {
        String code = """
                int func() {
                    try {
                        return 1;
                    } catch {
                        return 2;
                    } finally {
                        return 3;
                    }
                }
                
                intStorage.add(func());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3));
    }

    @Test
    public void tryCatchControlFlowTest() {
        String code = """
                void noop() {}
                int func1() {
                    try {
                        return 1;
                    } catch {
                        noop();
                    }
                }
                int func2() {
                    try {
                        noop();
                    } catch {
                        return 1;
                    }
                }
                int func3() {
                    try {
                        noop();
                    } catch {
                        noop();
                    }
                }
                int func4() {
                    try {
                        return 1;
                    } catch {
                        return 2;
                    }
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(8, 1, 93, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(15, 1, 173, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(22, 1, 251, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void tryFinallyControlFlowTest() {
        String code = """
                void noop() {}
                int func1() {
                    try {
                        return 1;
                    } finally {
                        noop();
                    }
                }
                int func2() {
                    try {
                        noop();
                    } finally {
                        return 1;
                    }
                }
                int func3() {
                    try {
                        noop();
                    } finally {
                        noop();
                    }
                }
                int func4() {
                    try {
                        return 1;
                    } finally {
                        return 2;
                    }
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(22, 1, 257, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void tryCatchFinallyControlFlowTest() {
        String code = """
                void noop() {}
                int func1() {
                    try {
                        noop();
                    } catch {
                        noop();
                    } finally {
                        noop();
                    }
                }
                int func2() {
                    try {
                        return 1;
                    } catch {
                        noop();
                    } finally {
                        noop();
                    }
                }
                int func3() {
                    try {
                        noop();
                    } catch {
                        return 1;
                    } finally {
                        noop();
                    }
                }
                int func4() {
                    try {
                        noop();
                    } catch {
                        noop();
                    } finally {
                        return 1;
                    }
                }
                int func5() {
                    try {
                        noop();
                    } catch {
                        return 1;
                    } finally {
                        return 2;
                    }
                }
                int func6() {
                    try {
                        return 1;
                    } catch {
                        noop();
                    } finally {
                        return 2;
                    }
                }
                int func7() {
                    try {
                        return 1;
                    } catch {
                        return 2;
                    } finally {
                        noop();
                    }
                }
                int func8() {
                    try {
                        return 1;
                    } catch {
                        return 2;
                    } finally {
                        return 3;
                    }
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(10, 1, 123, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(19, 1, 235, 1)),
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new SingleLineTextRange(28, 1, 347, 1))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}