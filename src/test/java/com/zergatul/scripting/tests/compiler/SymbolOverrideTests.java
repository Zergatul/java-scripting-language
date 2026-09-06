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
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        String code =
                "class ConfiguredClass {}\n" +
                "typealias ConfiguredAlias = int;\n" +
                "int ConfiguredFunction() => 30;\n" +
                "static int ConfiguredStatic = 40;\n" +
                "\n" +
                "ConfiguredClass object = new ConfiguredClass();\n" +
                "ConfiguredAlias value = 20;\n" +
                "intStorage.add(value);\n" +
                "intStorage.add(ConfiguredFunction());\n" +
                "intStorage.add(ConfiguredStatic);\n";

        Runnable program = compile(
                code,
                ConfiguredClass.class,
                ConfiguredAlias.class,
                ConfiguredFunction.class,
                ConfiguredStatic.class);
        program.run();

        Assertions.assertIterableEquals(Lists.of(20, 30, 40), ApiRoot.intStorage.list);
    }

    @Test
    public void localSymbolOverridesConfiguredType() {
        String code =
                "int ConfiguredLocal = 123;\n" +
                "intStorage.add(ConfiguredLocal);\n";

        Runnable program = compile(code, ConfiguredLocal.class);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateConfiguredTypeNamesAreRejected() {
        Assertions.assertThrows(
                InternalException.class,
                () -> new CompilationParametersBuilder()
                        .setRoot(ApiRoot.class)
                        .addCustomTypes(Lists.of(DuplicateConfiguredType1.class, DuplicateConfiguredType2.class))
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
                                "class Item {}\n" +
                                "class ⟪Item⟫ {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and type alias",
                                "class Item {}\n" +
                                "typealias ⟪Item⟫ = int;\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "duplicate type aliases",
                                "typealias Item = int;\n" +
                                "typealias ⟪Item⟫ = string;\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and function",
                                "class Item {}\n" +
                                "void ⟪Item⟫() {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "type alias and function",
                                "typealias Item = int;\n" +
                                "void ⟪Item⟫() {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "class and static variable",
                                "class Item {}\n" +
                                "static int ⟪Item⟫;\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"),
                        new DiagnosticCase(
                                "function and static variable",
                                "void Item() {}\n" +
                                "static int ⟪Item⟫;\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "Item"))
                .map(test -> DynamicTest.dynamicTest(
                        test.name(),
                        () -> assertDiagnostic(test.code(), test.error(), test.parameters())));
    }

    @Test
    public void compilationUnitSymbolsOverrideApiSymbols() {
        String code =
                "class ApiClass {}\n" +
                "typealias ApiAlias = int;\n" +
                "int ApiFunction() => 10;\n" +
                "static int ApiStatic = 20;\n" +
                "\n" +
                "ApiClass object = new ApiClass();\n" +
                "ApiAlias value = 30;\n" +
                "intStorage.add(ApiFunction());\n" +
                "intStorage.add(ApiStatic);\n" +
                "intStorage.add(value);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void functionOverloadsRemainValid() {
        String code =
                "int value(int x) => x;\n" +
                "int value(int x, int y) => x + y;\n" +
                "\n" +
                "intStorage.add(value(10));\n" +
                "intStorage.add(value(20, 30));\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 50), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateFunctionSignatureRemainsInvalid() {
        assertDiagnostic(
                "int value(int x) => x;\n" +
                "int ⟪value⟫(int y) => y;\n",
                BinderErrors.FunctionAlreadyDeclared);
    }

    @Test
    public void localsCanShadowCompilationUnitSymbols() {
        String code =
                "int functionValue() => 1;\n" +
                "static int staticValue = 2;\n" +
                "\n" +
                "int functionValue = 10;\n" +
                "int staticValue = 20;\n" +
                "int apiValue = 30;\n" +
                "\n" +
                "intStorage.add(functionValue);\n" +
                "intStorage.add(staticValue);\n" +
                "intStorage.add(apiValue);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void functionParameterCanShadowCompilationUnitSymbol() {
        String code =
                "int get(int apiValue) => apiValue;\n" +
                "intStorage.add(get(123));\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
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

        Assertions.assertIterableEquals(Lists.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void foreachVariableCanShadowCompilationUnitSymbol() {
        String code =
                "foreach (int loopValue in [1, 2]) {\n" +
                "    intStorage.add(loopValue);\n" +
                "}\n" +
                "intStorage.add(loopValue);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 200), ApiRoot.intStorage.list);
    }

    @Test
    public void declarationPatternCanShadowCompilationUnitSymbol() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "Object object = \"abc\";\n" +
                "if (object is string patternValue) {\n" +
                "    intStorage.add(patternValue.length);\n" +
                "}\n" +
                "intStorage.add(patternValue);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(3, 300), ApiRoot.intStorage.list);
    }

    @Test
    public void catchVariableCanShadowCompilationUnitSymbol() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "try {\n" +
                "    throw new RuntimeException();\n" +
                "} catch (catchValue) {\n" +
                "    intStorage.add(1);\n" +
                "}\n" +
                "intStorage.add(catchValue);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 400), ApiRoot.intStorage.list);
    }

    @TestFactory
    public Stream<DynamicTest> overlappingLocalConflicts() {
        return Stream.of(
                        new DiagnosticCase(
                                "local in the same scope",
                                "int value;\n" +
                                "int ⟪value⟫;\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "local in a nested scope",
                                "int value;\n" +
                                "{\n" +
                                "    int ⟪value⟫;\n" +
                                "}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "foreach variable",
                                "int value;\n" +
                                "foreach (int ⟪value⟫ in [1]) {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "declaration-pattern variable",
                                "typealias Object = Java<java.lang.Object>;\n" +
                                "int value;\n" +
                                "Object object = \"abc\";\n" +
                                "if (object is string ⟪value⟫) {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"),
                        new DiagnosticCase(
                                "catch variable",
                                "int value;\n" +
                                "try {} catch (⟪value⟫) {}\n",
                                BinderErrors.SymbolAlreadyDeclared,
                                "value"))
                .map(test -> DynamicTest.dynamicTest(
                        test.name(),
                        () -> assertDiagnostic(test.code(), test.error(), test.parameters())));
    }

    @Test
    public void siblingScopesCanReuseLocalName() {
        String code =
                "{\n" +
                "    int value = 10;\n" +
                "    intStorage.add(value);\n" +
                "}\n" +
                "{\n" +
                "    int value = 20;\n" +
                "    intStorage.add(value);\n" +
                "}\n" +
                "int value = 30;\n" +
                "intStorage.add(value);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 20, 30), ApiRoot.intStorage.list);
    }

    @Test
    public void lambdaParameterCanShadowEnclosingFunctionLocal() {
        String code =
                "int value = 10;\n" +
                "run.onInteger(value => intStorage.add(value));\n" +
                "run.triggerInteger(20);\n" +
                "intStorage.add(value);\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(20, 10), ApiRoot.intStorage.list);
    }

    @Test
    public void duplicateLambdaParametersAreInvalid() {
        assertDiagnostic(
                "run.onIntString((value, ⟪value⟫) => {});",
                BinderErrors.SymbolAlreadyDeclared,
                "value");
    }

    @Test
    public void classMethodOverloadsRemainValid() {
        String code =
                "class Calculator {\n" +
                "    int get(int value) => value;\n" +
                "    int get(int x, int y) => x + y;\n" +
                "}\n" +
                "\n" +
                "Calculator calculator = new Calculator();\n" +
                "intStorage.add(calculator.get(10));\n" +
                "intStorage.add(calculator.get(20, 30));\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 50), ApiRoot.intStorage.list);
    }

    @Test
    public void virtualMethodOverrideRemainsValid() {
        String code =
                "class Base {\n" +
                "    virtual int get() => 10;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    override int get() => base.get() + 20;\n" +
                "}\n" +
                "\n" +
                "Base value = new Child();\n" +
                "intStorage.add(value.get());\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(30), ApiRoot.intStorage.list);
    }

    @Test
    public void overridingMethodStillRequiresOverrideModifier() {
        String code =
                "class Base {\n" +
                "    virtual int get() => 10;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    int ⟪get⟫() => 20;\n" +
                "}\n";
        assertDiagnostic(code, BinderErrors.OverrideMissing);
    }

    @Test
    public void inheritedFieldShadowingRemainsInvalid() {
        String code =
                "class Base {\n" +
                "    int value;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    int ⟪value⟫;\n" +
                "}\n";
        assertDiagnostic(code, BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void extensionMethodCannotReplaceInstanceMethod() {
        String code =
                "class Item {\n" +
                "    int get(int value) => value;\n" +
                "}\n" +
                "extension(Item) {\n" +
                "    int ⟪get⟫(int value) => value + 1;\n" +
                "}\n";
        assertDiagnostic(code, BinderErrors.MethodAlreadyDeclared);
    }

    @Test
    public void extensionMethodCanOverloadInstanceMethod() {
        String code =
                "class Item {\n" +
                "    int get(int value) => value;\n" +
                "}\n" +
                "extension(Item) {\n" +
                "    int get(string value) => value.length;\n" +
                "}\n" +
                "\n" +
                "Item item = new Item();\n" +
                "intStorage.add(item.get(10));\n" +
                "intStorage.add(item.get(\"abc\"));\n";

        Runnable program = compile(code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void functionNameSameAsFunctionInterfaceMethodTest() {
        String code =
                "void run() {\n" +
                "    [0][1] = 2;\n" +
                "}\n" +
                "run();\n";

        Runnable program = compile(code);
        try {
            program.run();
        } catch (IndexOutOfBoundsException e) {
            Assertions.assertEquals("1", e.getMessage());
            CompilerHelper.assertTopStackTrace(
                    Lists.of(
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
        builder.addCustomTypes(Lists.of(customTypes));
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
                Lists.of(new DiagnosticMessage(error, marked.getRange(MARK), parameters)),
                result.getDiagnostics());
    }

    private static final class DiagnosticCase {

        private final String name;
        private final String code;
        private final ErrorCode error;
        private final Object[] parameters;

        private DiagnosticCase(String name, String code, ErrorCode error, Object... parameters) {
            this.name = name;
            this.code = code;
            this.error = error;
            this.parameters = parameters;
        }

        public String name() {
            return name;
        }

        public String code() {
            return code;
        }

        public ErrorCode error() {
            return error;
        }

        public Object[] parameters() {
            return parameters;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            DiagnosticCase that = (DiagnosticCase) obj;
            return  Objects.equals(this.name, that.name) &&
                    Objects.equals(this.code, that.code) &&
                    Objects.equals(this.error, that.error) &&
                    Arrays.equals(this.parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, code, error, Arrays.hashCode(parameters));
        }

        @Override
        public String toString() {
            return  "DiagnosticCase[" +
                    "name=" + name + ", " +
                    "code=" + code + ", " +
                    "error=" + error + ", " +
                    "parameters=" + Arrays.toString(parameters) + ']';
        }
    }

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