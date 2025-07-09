package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class TypeCastExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void boolTest() {
        String code = """
                boolStorage.add(false as boolean);
                boolStorage.add(true as boolean);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true));
    }

    @Test
    public void intTest() {
        String code = """
                intStorage.add(0 as int);
                intStorage.add(1 as int);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 1));
    }

    @Test
    public void arrayTest() {
        String code = """
                foreach (let val in [1, 2, 3] as int[]) {
                    intStorage.add(val);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
    }

    @Test
    public void castExceptionTest() {
        String code = """
                let x = 1 as float;
                """;

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(ClassCastException.class, program::run);
    }

    @Test
    public void customTypesTest1() {
        String code = """
                let o1 = api.getBaseType();
                let o2 = api.getChildType();
                if (o1 is ChildType) {
                    intStorage.add((o1 as ChildType).getNumber());
                } else {
                    intStorage.add(100 - (o1 as BaseType).getNumber());
                }
                if (o2 is ChildType) {
                    intStorage.add((o2 as ChildType).getNumber());
                    intStorage.add((o2 as ChildType).getCount());
                }
                intStorage.add(o1.getNumber() + o2.getNumber());
                """;

        Runnable program = compileWithCustomTypes(ApiRoot.class, code, BaseType.class, ChildType.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(90, 20, 1, 30));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {
        public BaseType getBaseType() {
            return new BaseType();
        }
        public BaseType getChildType() {
            return new ChildType();
        }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "BaseType")
    public static class BaseType {
        public int getNumber() { return 10; }
    }

    @SuppressWarnings("unused")
    @CustomType(name = "ChildType")
    public static class ChildType extends BaseType {
        @Override
        public int getNumber() { return 20; }
        public int getCount() { return 1; }
    }
}