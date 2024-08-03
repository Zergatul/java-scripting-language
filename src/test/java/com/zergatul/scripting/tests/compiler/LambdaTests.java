package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.compiler.helpers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class LambdaTests {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                run.skip(() => {
                    intStorage.add(20);
                });
                run.once(() => {
                    intStorage.add(10);
                    intStorage.add(5);
                });
                run.multiple(3, () => {
                    intStorage.add(2);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 5, 2, 2, 2));
    }

    @Test
    public void noBlock1Test() {
        String code = """
                run.once(() => intStorage.add(120));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(120));
    }

    @Test
    public void noBlock2Test() {
        String code = """
                static int x = 100;
                run.once(() => x = 200);
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(200));
    }

    @Test
    public void noBlock3Test() {
        String code = """
                static int x = 100;
                run.once(() => x++);
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(101));
    }

    @Test
    public void noBlock4Test() {
        String code = """
                static int x = 100;
                run.once(() => x--);
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(99));
    }

    @Test
    public void noBlock5Test() {
        String code = """
                static int x = 100;
                run.once(() => x *= 3);
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(300));
    }

    @Test
    public void noUnboxingTest() {
        String code = """
                run.onString(str => stringStorage.add("1. " + str));
                run.onString(str => stringStorage.add("2. " + str));
                run.triggerString("qwerty");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("1. qwerty", "2. qwerty"));
    }

    @Test
    public void unboxBooleanTest() {
        String code = """
                run.onBoolean(b => intStorage.add(b ? 2 : 1));
                run.onBoolean(b => intStorage.add(b ? 5 : 4));
                run.triggerBoolean(false);
                run.triggerBoolean(true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 4, 2, 5));
    }

    @Test
    public void unboxIntTest() {
        String code = """
                run.onInteger(i => intStorage.add(i + 1));
                run.onInteger(i => intStorage.add(i + 2));
                run.triggerInteger(100);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(101, 102));
    }

    @Test
    public void unboxFloatTest() {
        String code = """
                run.onFloat(v => floatStorage.add(v + 0.5));
                run.onFloat(v => floatStorage.add(v + 0.25));
                run.triggerFloat(1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.5, 1.25));
    }

    @Test
    public void twoParamsTest() {
        String code = """
                run.onIntString((i, s) => floatStorage.add(i + 0.5));
                run.onIntString((i, s) => stringStorage.add("$" + s));
                run.triggerIntString(1, "a");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.5));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("$a"));
    }

    @Test
    public void returnContextTest() {
        String code = """
                int a = 123;
                run.once(() => intStorage.add(321));
                intStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(321, 123));
    }

    @Test
    public void simpleFunctionTest() {
        String code = """
                intStorage.add(run.sumInts(10, () => 10));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
    }

    @Test
    public void simpleFunctionCastTest() {
        String code = """
                floatStorage.add(run.sumFloats(5, () => 10));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(50.0));
    }

    @Test
    public void simpleFunctionCannotCastTest() {
        String code = """
                run.sumInts(123, () => 123.0);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(messages.get(0).code, BinderErrors.CannotImplicitlyConvert.code());
    }

    @Test
    public void map1Test() {
        String code = """
                int[] array = run.map(new int[] { 1, 2, 3, 4, 5 }, i => i * 2);
                foreach (int i in array) intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 6, 8, 10));
    }

    @Test
    public void map2Test() {
        String code = """
                int x2(int value) { return value * 2; }
                
                int[] array = run.map(new int[] { 1, 2, 3, 4, 5 }, x2);
                foreach (int i in array) intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 6, 8, 10));
    }

    @Test
    public void reduce1Test() {
        String code = """
                int result = run.reduce(new int[] { 1, 2, 3, 4, 5 }, 100, (acc, value) => acc + value * value);
                intStorage.add(result);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(155));
    }

    @Test
    public void reduce2Test() {
        String code = """
                int func(int acc, int value) { return acc + value * value; }
                
                int result = run.reduce(new int[] { 1, 2, 3, 4, 5 }, 100, func);
                intStorage.add(result);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(155));
    }

    @Test
    public void captureInt1Test() {
        String code = """
                int a = 100;
                a++;
                intStorage.add(a);
                run.once(() => a = 200);
                intStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(101, 200));
    }

    @Test
    public void captureInt2Test() {
        String code = """
                int a = 100;
                run.once(() => {
                    run.once(() => {
                        a = 200;
                    });
                });
                intStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(200));
    }

    @Test
    public void captureInt3Test() {
        String code = """
                int a = 100;
                run.once(() => {
                    int b = 200;
                    run.once(() => {
                        a += b;
                    });
                });
                intStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(300));
    }

    @Test
    public void captureInt4Test() {
        String code = """
                int a = 1;
                int b = 2;
                int c = 3;
                int d = 4;
                int e = 5;
                run.once(() => {
                    a++;
                    run.once(() => {
                        b++;
                        run.once(() => {
                            c++;
                            run.once(() => {
                                d++;
                                run.once(() => {
                                    e++;
                                });
                            });
                        });
                    });
                });
                intStorage.add(a);
                intStorage.add(b);
                intStorage.add(c);
                intStorage.add(d);
                intStorage.add(e);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 3, 4, 5, 6));
    }

    @Test
    public void captureInt5Test() {
        String code = """
                int x;
                run.multiple(1, () => {
                    run.multiple(2, () => {
                        run.multiple(3, () => {
                            run.multiple(4, () => {
                                run.multiple(5, () => {
                                    x++;
                                });
                            });
                        });
                    });
                });
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(120));
    }

    @Test
    public void captureInt6Test() {
        String code = """
                int x1 = 1;
                run.multiple(1, () => {
                    int x2 = 2;
                    run.multiple(2, () => {
                        int x3 = 3;
                        run.multiple(3, () => {
                            int x4 = 4;
                            run.multiple(4, () => {
                                int x5 = 5;
                                run.multiple(5, () => {
                                    x1++;
                                    x2++;
                                    x3++;
                                    x4++;
                                    x5++;
                                });
                                x1 += x5;
                            });
                            x1 += x4;
                        });
                        x1 += x3;
                    });
                    x1 += x2;
                });
                intStorage.add(x1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(753));
    }

    @Test
    public void captureFloatTest() {
        String code = """
                float x;
                run.multiple(1, () => {
                    run.multiple(2, () => {
                        run.multiple(3, () => {
                            run.multiple(4, () => {
                                run.multiple(5, () => {
                                    x = x + 1;
                                });
                            });
                        });
                    });
                });
                floatStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(120.0));
    }

    @Test
    public void captureBooleanTest() {
        String code = """
                boolean b;
                run.multiple(1, () => {
                    run.multiple(3, () => {
                        run.multiple(5, () => {
                            run.multiple(7, () => {
                                run.multiple(9, () => {
                                    b = !b;
                                });
                            });
                        });
                    });
                });
                boolStorage.add(b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true));
    }

    @Test
    public void captureMultipleTest() {
        String code = """
                int a1 = 1;
                int a2 = 2;
                int a3 = 3;
                int sum;
                run.once(() => {
                    int b1 = 4;
                    int b2 = 5;
                    int b3 = 6;
                    run.once(() => {
                        sum += a1;
                        sum += a2;
                        sum += a3;
                        sum += b1;
                        sum += b2;
                        sum += b3;
                    });
                });
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21));
    }

    @Test
    public void failedArguments1Test() {
        String code = """
                bad.run(() => intStorage.add(1));
                """;

        List<DiagnosticMessage> diagnostics = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(
                diagnostics,
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.CannotCastArguments,
                                new SingleLineTextRange(1, 8, 7, 25))));
    }

    @Test
    public void failedArguments2Test() {
        String code = """
                run.once(10, () => {});
                """;

        List<DiagnosticMessage> diagnostics = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(
                diagnostics,
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NoOverloadedMethods,
                                new SingleLineTextRange(1, 1, 0, 8),
                                "once",
                                2)));
    }

    @Test
    public void failedArguments3Test() {
        String code = """
                run.multiple("10", () => {});
                """;

        List<DiagnosticMessage> diagnostics = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(
                diagnostics,
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.CannotCastArguments,
                                new SingleLineTextRange(1, 13, 12, 16))));
    }

    @Test
    public void failedArguments4Test() {
        String code = """
                run.multiple(10, (x) => {});
                """;

        List<DiagnosticMessage> diagnostics = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(
                diagnostics,
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.CannotCastArguments,
                                new SingleLineTextRange(1, 13, 12, 15))));
    }

    @Test
    public void failedArgument5Test() {
        String code = """
                run.multiple(10, (x) => {});
                """;

        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, new CompilationParametersBuilder().setRoot(ApiRoot.class).build());
        BinderOutput binderOutput = binder.bind();

        List<BoundStatementNode> statements = binderOutput.unit().statements.statements;
        Assertions.assertEquals(statements.size(), 1);

        BoundExpressionStatementNode statement = (BoundExpressionStatementNode) binderOutput.unit().statements.statements.get(0);
        BoundMethodInvocationExpressionNode invocation = (BoundMethodInvocationExpressionNode) statement.expression;
        List<BoundExpressionNode> arguments = invocation.arguments.arguments;
        Assertions.assertEquals(arguments.size(), 2);

        Assertions.assertEquals(arguments.get(0),
                new BoundIntegerLiteralExpressionNode(10, new SingleLineTextRange(1, 14, 13, 2)));

        Assertions.assertEquals(arguments.get(1),
                new BoundInvalidExpressionNode(new SingleLineTextRange(1, 18, 17, 9)));
    }

    // TODO: capture function parameters?
    // maybe not allow!

    public static class ApiRoot {
        public static Run run;
        public static BoolStorage boolStorage = new BoolStorage();
        public static IntStorage intStorage = new IntStorage();
        public static FloatStorage floatStorage = new FloatStorage();
        public static StringStorage stringStorage = new StringStorage();
        public static Bad bad = new Bad();
    }

    public static class Bad {
        public void run(Runnable runnable) {
            runnable.run();
        }
    }
}