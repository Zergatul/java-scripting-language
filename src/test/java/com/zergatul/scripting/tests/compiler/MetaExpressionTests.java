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

public class MetaExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest() {
        String code = """
                boolStorage.add(#typeof(false) == #type(boolean));
                boolStorage.add(#typeof(1) == #type(int));
                boolStorage.add(#typeof('a') == #type(char));
                boolStorage.add(#typeof(3000000000L) == #type(int64));
                boolStorage.add(#typeof(0.0) == #type(float));
                boolStorage.add(#typeof("") == #type(string));
                
                boolStorage.add(#typeof(1.1) == #type(int));
                boolStorage.add(#typeof(1.1) != #type(int));
                boolStorage.add(#typeof(boolStorage) == #typeof(boolStorage));
                boolStorage.add(#typeof(boolStorage) == #typeof(intStorage));
                
                boolStorage.add(#typeof(api.getA()) == #type(TypeA));
                boolStorage.add(#typeof(api.getB()) == #type(TypeB));
                boolStorage.add(#typeof(api.getC()) == #type(TypeC));
                boolStorage.add(#typeof(api.getB()) == #type(TypeA));
                boolStorage.add(#typeof(api.getC()) == #type(TypeA));
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, TypeA.class, TypeB.class, TypeC.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(
                true, true, true, true, true, true,
                false, true, true, false,
                true, true, true, false, false));
    }

    @Test
    public void typeNameTest() {
        String code = """
                stringStorage.add(#typeof(123).name);
                stringStorage.add(#type(int).name);
                stringStorage.add(#typeof("").name);
                stringStorage.add(#type(string).name);
                stringStorage.add(#typeof(api.getA()).name);
                stringStorage.add(#typeof(api.getB()).name);
                stringStorage.add(#typeof(api.getC()).name);
                stringStorage.add(#typeof(api).name);
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, TypeA.class, TypeB.class, TypeC.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(
                "int", "int",
                "string", "string",
                "TypeA", "TypeB", "TypeC",
                "Java<com.zergatul.scripting.tests.compiler.MetaExpressionTests$Api>"));
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
}