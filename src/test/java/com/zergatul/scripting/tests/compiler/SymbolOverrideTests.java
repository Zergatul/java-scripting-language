package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.ErrorCode;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.tests.compiler.helpers.CompilerHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.utility.MarkedCode;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines the intended identifier precedence rules:
 * <ol>
 *     <li>API-root symbols and configured custom types are overridable imports.</li>
 *     <li>Script compilation-unit symbols share one namespace and cannot shadow each other.</li>
 *     <li>Locals may shadow compilation-unit symbols, but not overlapping locals in the same function.</li>
 *     <li>A lambda creates a new function boundary and may shadow locals from its enclosing function.</li>
 *     <li>Members may overload by signature, but field/method hiding remains forbidden.</li>
 * </ol>
 */
public class SymbolOverrideTests extends ComparatorTest {

    private static final String MARK = "⟪⟫";

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.run = new Run();
        ApiRoot.apiValue = 100;
        ApiRoot.loopValue = 200;
        ApiRoot.patternValue = 300;
        ApiRoot.catchValue = 400;
    }

    @Test
    public void compilationUnitSymbolsOverrideConfiguredTypes() {
        String code = """
                class ConfiguredClass {}
                typealias ConfiguredAlias = int;
                int ConfiguredFunction() => 30;
                static int ConfiguredStatic = 40;

                ConfiguredClass object = new ConfiguredClass();
                ConfiguredAlias value = 20;
                intStorage.add(value);
                intStorage.add(ConfiguredFunction());
                intStorage.add(ConfiguredStatic);
                """;

        Runnable program = compile(
                code,
                ConfiguredClass.class,
                ConfiguredAlias.class,
                ConfiguredFunction.class,
                ConfiguredStatic.class);
        program.run();

        Assertions.assertIterableEquals(List.of(20, 30, 40), ApiRoot.intStorage.list);
    }

    @Test
    public void localSymbolOverridesConfiguredType() {
        String code = """
                int ConfiguredLocal = 123;
                intStorage.add(ConfiguredLocal);
                """;

        Runnable program = compile(code, ConfiguredLocal.class);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateConfiguredTypeNamesAreRejected() {
        Assertions.assertThrows(
                InternalException.class,
                () -> new CompilationParametersBuilder()
                        .setRoot(ApiRoot.class)
                        .addCustomTypes(List.of(DuplicateConfiguredType1.class, DuplicateConfiguredType2.class))
                        .build());
    }

    @Test
    public void configuredTypeCannotReplacePredefinedRuntimeType() {
        Assertions.assertThrows(
                InternalException.class,
                () -> new CompilationParametersBuilder()
                        .setRoot(ApiRoot.class)
                        .addCustomType(ConfiguredRuntimeType.class)
                        .build());
    }

    @TestFactory
    public Stream<DynamicTest> compilationUnitSymbolConflicts() {
        return Stream.of(
                        new DiagnosticCase(
                                "duplicate classes",
                                """
                                        class Item {}
                                        class ⟪Item⟫ {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and type alias",
                                """
                                        class Item {}
                                        typealias ⟪Item⟫ = int;
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "duplicate type aliases",
                                """
                                        typealias Item = int;
                                        typealias ⟪Item⟫ = string;
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and function",
                                """
                                        class Item {}
                                        void ⟪Item⟫() {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "type alias and function",
                                """
                                        typealias Item = int;
                                        void ⟪Item⟫() {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and static variable",
                                """
                                        class Item {}
                                        static int ⟪Item⟫;
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "function and static variable",
                                """
                                        void Item() {}
                                        static int ⟪Item⟫;
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"))
                .map(test -> DynamicTest.dynamicTest(
                        test.name(),
                        () -> assertDiagnostic(test.code(), test.error(), test.parameters())));
    }

    @Test
    public void compilationUnitSymbolsOverrideApiSymbols() {
        String code = """
                class ApiClass {}
                typealias ApiAlias = int;
                int ApiFunction() => 10;
                static int ApiStatic = 20;

                ApiClass object = new ApiClass();
                ApiAlias value = 30;
                intStorage.add(ApiFunction());
                intStorage.add(ApiStatic);
                intStorage.add(value);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void functionOverloadsRemainValid() {
        String code = """
                int value(int x) => x;
                int value(int x, int y) => x + y;

                intStorage.add(value(10));
                intStorage.add(value(20, 30));
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 50), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateFunctionSignatureRemainsInvalid() {
        assertDiagnostic(
                """
                        int value(int x) => x;
                        int ⟪value⟫(int y) => y;
                        """,
                BinderErrors.FunctionAlreadyDeclared);
    }

    @Test
    public void localsCanShadowCompilationUnitSymbols() {
        String code = """
                int functionValue() => 1;
                static int staticValue = 2;

                int functionValue = 10;
                int staticValue = 20;
                int apiValue = 30;

                intStorage.add(functionValue);
                intStorage.add(staticValue);
                intStorage.add(apiValue);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void functionParameterCanShadowCompilationUnitSymbol() {
        String code = """
                int get(int apiValue) => apiValue;
                intStorage.add(get(123));
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void externalParameterCanShadowApiSymbol() {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setInterface(InputAction.class)
                .build();
        CompilationResult result = new Compiler(parameters).compile("intStorage.add(apiValue);");

        Assertions.assertNull(result.getDiagnostics());
        Assertions.assertTrue(result.getProgram() instanceof InputAction);
        InputAction program = result.getProgram();
        program.invoke(321);

        Assertions.assertIterableEquals(List.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void foreachVariableCanShadowCompilationUnitSymbol() {
        String code = """
                foreach (int loopValue in [1, 2]) {
                    intStorage.add(loopValue);
                }
                intStorage.add(loopValue);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 200), ApiRoot.intStorage.list);
    }

    @Test
    public void declarationPatternCanShadowCompilationUnitSymbol() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                Object object = "abc";
                if (object is string patternValue) {
                    intStorage.add(patternValue.length);
                }
                intStorage.add(patternValue);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(3, 300), ApiRoot.intStorage.list);
    }

    @Test
    public void catchVariableCanShadowCompilationUnitSymbol() {
        String code = """
                typealias RuntimeException = Java<java.lang.RuntimeException>;
                try {
                    throw new RuntimeException();
                } catch (catchValue) {
                    intStorage.add(1);
                }
                intStorage.add(catchValue);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 400), ApiRoot.intStorage.list);
    }

    @TestFactory
    public Stream<DynamicTest> overlappingLocalConflicts() {
        return Stream.of(
                        new DiagnosticCase(
                                "local in the same scope",
                                """
                                        int value;
                                        int ⟪value⟫;
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "local in a nested scope",
                                """
                                        int value;
                                        {
                                            int ⟪value⟫;
                                        }
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "foreach variable",
                                """
                                        int value;
                                        foreach (int ⟪value⟫ in [1]) {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "declaration-pattern variable",
                                """
                                        typealias Object = Java<java.lang.Object>;
                                        int value;
                                        Object object = "abc";
                                        if (object is string ⟪value⟫) {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "catch variable",
                                """
                                        int value;
                                        try {} catch (⟪value⟫) {}
                                        """,
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"))
                .map(test -> DynamicTest.dynamicTest(
                        test.name(),
                        () -> assertDiagnostic(test.code(), test.error(), test.parameters())));
    }

    @Test
    public void siblingScopesCanReuseLocalName() {
        String code = """
                {
                    int value = 10;
                    intStorage.add(value);
                }
                {
                    int value = 20;
                    intStorage.add(value);
                }
                int value = 30;
                intStorage.add(value);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void lambdaParameterCanShadowEnclosingFunctionLocal() {
        String code = """
                int value = 10;
                run.onInteger(value => intStorage.add(value));
                run.triggerInteger(20);
                intStorage.add(value);
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(20, 10), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateLambdaParametersAreInvalid() {
        assertDiagnostic(
                """
                        run.onIntString((value, ⟪value⟫) => {});
                        """,
                BinderErrors.SymbolAlreadyDeclared,
                "value");
    }

    @Test
    public void classMethodOverloadsRemainValid() {
        String code = """
                class Calculator {
                    int get(int value) => value;
                    int get(int x, int y) => x + y;
                }

                Calculator calculator = new Calculator();
                intStorage.add(calculator.get(10));
                intStorage.add(calculator.get(20, 30));
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 50), ApiRoot.intStorage.list);
    }

    @Test
    public void virtualMethodOverrideRemainsValid() {
        String code = """
                class Base {
                    virtual int get() => 10;
                }
                class Child : Base {
                    override int get() => base.get() + 20;
                }

                Base value = new Child();
                intStorage.add(value.get());
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(30), ApiRoot.intStorage.list);
    }

    @Test
    public void overridingMethodStillRequiresOverrideModifier() {
        assertDiagnostic(
                """
                        class Base {
                            virtual int get() => 10;
                        }
                        class Child : Base {
                            int ⟪get⟫() => 20;
                        }
                        """,
                BinderErrors.OverrideMissing);
    }

    @Test
    public void inheritedFieldShadowingRemainsInvalid() {
        assertDiagnostic(
                """
                        class Base {
                            int value;
                        }
                        class Child : Base {
                            int ⟪value⟫;
                        }
                        """,
                BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void extensionMethodCannotReplaceInstanceMethod() {
        assertDiagnostic(
                """
                        class Item {
                            int get(int value) => value;
                        }
                        extension(Item) {
                            int ⟪get⟫(int value) => value + 1;
                        }
                        """,
                BinderErrors.MethodAlreadyDeclared);
    }

    @Test
    public void extensionMethodCanOverloadInstanceMethod() {
        String code = """
                class Item {
                    int get(int value) => value;
                }
                extension(Item) {
                    int get(string value) => value.length;
                }

                Item item = new Item();
                intStorage.add(item.get(10));
                intStorage.add(item.get("abc"));
                """;

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void functionNameSameAsFunctionInterfaceMethodTest() {
        String code = """
                void run() {
                    [0][1] = 2;
                }
                run();
                """;

        Runnable program = compile(code);
        try {
            program.run();
        } catch (IndexOutOfBoundsException e) {
            Assertions.assertEquals("Index 1 out of bounds for length 1", e.getMessage());
            CompilerHelper.assertTopStackTrace(
                    List.of(
                            new StackTraceElement("com.zergatul.scripting.dynamic.Script", "$function$run", null, -1),
                            new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", null, -1)),
                    Arrays.asList(e.getStackTrace()));
            return;
        }

        Assertions.fail();
    }

    private Runnable compile(String code, Class<?>... customTypes) {
        CompilationParametersBuilder builder = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .emitVariableNames(true);
        builder.addCustomTypes(List.of(customTypes));
        CompilationResult result = new Compiler(builder.build()).compile(code);

        Assertions.assertNull(result.getDiagnostics());
        Assertions.assertTrue(result.getProgram() instanceof Runnable);
        return result.getProgram();
    }

    private void assertDiagnostic(String text, ErrorCode error, Object... parameters) {
        MarkedCode marked = MarkedCode.from(text);
        CompilationResult result = new Compiler(
                new CompilationParametersBuilder()
                        .setRoot(ApiRoot.class)
                        .build())
                .compile(marked.getCode());

        Assertions.assertNull(result.getProgram());
        comparator.assertEquals(
                List.of(new DiagnosticMessage(error, marked.getRange(MARK), parameters)),
                result.getDiagnostics());
    }

    private record DiagnosticCase(String name, String code, ErrorCode error, Object... parameters) {}

    @FunctionalInterface
    public interface InputAction {
        void invoke(int apiValue);
    }

    @SuppressWarnings("unused")
    public static class ApiRoot {
        public static IntStorage intStorage;
        public static Run run;
        public static int apiValue;
        public static int loopValue;
        public static int patternValue;
        public static int catchValue;
        public static Object ApiFunction;
        public static Object ApiStatic;
        public static Object ApiClass;
        public static Object ApiAlias;
    }

    @CustomType(name = "ConfiguredClass")
    public static class ConfiguredClass {}

    @CustomType(name = "ConfiguredAlias")
    public static class ConfiguredAlias {}

    @CustomType(name = "ConfiguredFunction")
    public static class ConfiguredFunction {}

    @CustomType(name = "ConfiguredStatic")
    public static class ConfiguredStatic {}

    @CustomType(name = "ConfiguredLocal")
    public static class ConfiguredLocal {}

    @CustomType(name = "DuplicateConfiguredType")
    public static class DuplicateConfiguredType1 {}

    @CustomType(name = "DuplicateConfiguredType")
    public static class DuplicateConfiguredType2 {}

    @CustomType(name = "Type")
    public static class ConfiguredRuntimeType {}
}