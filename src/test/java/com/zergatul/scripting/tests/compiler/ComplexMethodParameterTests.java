package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.utility.MarkedCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ComplexMethodParameterTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.test = new TestApi();
    }

    @Test
    public void unaryOperatorMethodFailedTest() {
        MarkedCode marked = MarkedCode.from("""
                test.getAttachmentTarget().modifyAttached⟦(1, 2)⟧;
                """);

        String candidates = """
                Candidates:
                Java<java.lang.Object> modifyAttached(Java<com.zergatul.scripting.tests.compiler.ComplexMethodParameterTests$AttachmentType> type, Java<java.util.function.UnaryOperator> modifier)""";

        comparator.assertDiagnostics(
                ApiRoot.class, marked, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "modifyAttached", candidates);
    }

    @Test
    public void unaryOperatorMethodSuccessTest() {
        String code = """
                typealias Supplier = Java<java.util.function.Supplier>;
                typealias UnaryOperator = Java<java.util.function.UnaryOperator>;
                
                class AttachmentImpl : Java<com.zergatul.scripting.tests.compiler.ComplexMethodParameterTests$AttachmentType> {
                    override Supplier initializer() => () => "bo";
                }
                
                let obj = test.getAttachmentTarget().modifyAttached(
                    new AttachmentImpl(),
                    input => #cast(input, string) + #cast(input, string));
                stringStorage.add(#cast(obj, string));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("bobo"), ApiRoot.stringStorage.list);
    }

    @Test
    public void recursiveComparableTypeParameterFailedTest() {
        MarkedCode marked = MarkedCode.from("""
                test.getValueTarget().getValueOrElse⟦(1, 2)⟧;
                """);

        String candidates = """
                Candidates:
                Java<java.lang.Comparable> getValueOrElse(Java<com.zergatul.scripting.tests.compiler.ComplexMethodParameterTests$Property> property, Java<java.lang.Comparable> defaultValue)""";

        comparator.assertDiagnostics(
                ApiRoot.class, marked, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "getValueOrElse", candidates);
    }

    @Test
    public void recursiveComparableTypeParameterSuccessTest() {
        String code = """
                typealias Comparable = Java<java.lang.Comparable>;
                
                class PropertyImpl : Java<com.zergatul.scripting.tests.compiler.ComplexMethodParameterTests$Property> {
                    Comparable value;
                    constructor(Comparable value) => this.value = value;
                    override Comparable get() => value;
                }
                
                let value1 = test.getValueTarget().getValueOrElse(new PropertyImpl("go"), "x");
                let value2 = test.getValueTarget().getValueOrElse(new PropertyImpl(null), "y");
                stringStorage.add(#cast(value1, string));
                stringStorage.add(#cast(value2, string));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("go", "y"), ApiRoot.stringStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static TestApi test;
    }

    @SuppressWarnings("unused")
    public static class TestApi {

        public AttachmentTarget getAttachmentTarget() {
            return new AttachmentTarget();
        }

        public ValueTarget getValueTarget() {
            return new ValueTarget();
        }
    }

    @SuppressWarnings("unused")
    public static class AttachmentTarget {
        public <A> A modifyAttached(AttachmentType<A> type, UnaryOperator<A> modifier) {
            return modifier.apply(type.initializer().get());
        }
    }

    @SuppressWarnings("unused")
    public interface AttachmentType<A> {
        Supplier<A> initializer();
    }

    @SuppressWarnings("unused")
    public static class ValueTarget {
        public <T extends Comparable<T>> T getValueOrElse(Property<T> property, T defaultValue) {
            if (property.get() != null) {
                return property.get();
            } else {
                return defaultValue;
            }
        }
    }

    @SuppressWarnings("unused")
    public abstract static class Property<T extends Comparable<T>> {
        public abstract T get();
    }
}