package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomTypes;

public class ObjectCreationTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest1() {
        String code = """
                let a = new TypeA();
                let b = new TypeB();
                let c = new TypeC();
                stringStorage.add(#typeof(a).name);
                stringStorage.add(#typeof(b).name);
                stringStorage.add(#typeof(c).name);
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, TypeA.class, TypeB.class, TypeC.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("TypeA", "TypeB", "TypeC"));
    }

    @Test
    public void basicTest2() {
        String code = """
                let a = new Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeD>();
                let b = new Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeE>();
                let c = new Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeF>();
                stringStorage.add(#typeof(a).name);
                stringStorage.add(#typeof(b).name);
                stringStorage.add(#typeof(c).name);
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(
                "Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeD>",
                "Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeE>",
                "Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeF>"));
    }

    @Test
    public void basicTest3() {
        String code = """
                let instance = new Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeH>(
                    new Java<com.zergatul.scripting.tests.compiler.ObjectCreationTests$TypeG>(123));
                intStorage.add(instance.g.value);
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    public static class Api {
        public TypeA getA() { return new TypeA(); }
        public TypeA getB() { return new TypeB(); }
        public TypeA getC() { return new TypeC(); }
    }

    @CustomType(name = "TypeA")
    public static class TypeA {}

    @CustomType(name = "TypeB")
    public static class TypeB extends TypeA {}

    @CustomType(name = "TypeC")
    public static class TypeC extends TypeB {}

    public static class TypeD {}

    public static class TypeE {}

    public static class TypeF {}

    public static class TypeG {

        public final int value;

        public TypeG(int value) {
            this.value = value;
        }
    }

    public static class TypeH {

        public final TypeG g;

        public TypeH(TypeG g) {
            this.g = g;
        }
    }
}