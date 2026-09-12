package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class StackFrameTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void customClassAndStringFrameMergeTest() {
        String code = """
                class A {}
                
                if (true) {
                    let x = new A();
                } else {
                    let y = "";
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void longAndStringFrameMergeTest() {
        String code = """
                class A {}
                
                if (true) {
                    long x = 1;
                } else {
                    let y = "";
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void longAndIntFrameMergeTest() {
        String code = """
                class A {}
                
                if (true) {
                    int64 x = 1;
                } else {
                    int32 y = 2;
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void currentClassAndForwardSubclassFrameMergeTest() {
        String code = """
                class Base {
                    public virtual int value() => 10;
                    public Base choose(boolean flag) {
                        Base result = this;
                        if (flag) {
                            result = new Child();
                        }
                        return result;
                    }
                }
                class Child : Base {
                    public override int value() => 20;
                }

                let instance = new Base();
                intStorage.add(instance.choose(false).value());
                intStorage.add(instance.choose(true).value());
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(10, 20), ApiRoot.intStorage.list);
    }

    @Test
    public void siblingClassesFrameMergeTest() {
        String code = """
                class Base {
                    public virtual int value() => 0;
                }
                class First : Base {
                    public override int value() => 1;
                }
                class Second : Base {
                    public override int value() => 2;
                }

                Base choose(boolean flag) {
                    Base result = new First();
                    if (flag) {
                        result = new Second();
                    }
                    return result;
                }

                intStorage.add(choose(false).value());
                intStorage.add(choose(true).value());
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void generatedArraysFrameMergeTest() {
        String code = """
                class Base {}
                class First : Base {}
                class Second : Base {}

                void test(boolean flag) {
                    if (flag) {
                        let first = new First[1];
                        intStorage.add(first.length);
                    } else {
                        let second = new Second[2];
                        intStorage.add(second.length);
                    }
                }

                test(true);
                test(false);
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void commonJavaSuperclassFrameMergeTest() {
        String code = """
                class First : Java<java.util.ArrayList> {}
                class Second : Java<java.util.LinkedList> {}

                void test(boolean flag) {
                    Java<java.util.AbstractList> result = new First();
                    if (flag) {
                        result = new Second();
                    }
                    result.add("item");
                    intStorage.add(result.size());
                }

                test(false);
                test(true);
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(1, 1), ApiRoot.intStorage.list);
    }

    @Test
    public void inheritedJavaInterfaceFrameMergeTest() {
        String code = """
                class Base : Java<java.lang.Runnable> {
                    public override void run() => intStorage.add(1);
                }
                class Child : Base {}

                void test(boolean flag, Java<java.lang.Runnable> input) {
                    Java<java.lang.Runnable> result = input;
                    if (flag) {
                        result = new Child();
                    }
                    result.run();
                }

                test(false, () => intStorage.add(2));
                test(true, () => intStorage.add(2));
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(2, 1), ApiRoot.intStorage.list);
    }

    @Test
    public void capturedLambdasFrameMergeTest() {
        String code = """
                void test(boolean flag) {
                    let value = 10;
                    fn<() => int> result = () => value + 1;
                    if (flag) {
                        result = () => value + 2;
                    }
                    intStorage.add(result());
                }

                test(false);
                test(true);
                """;

        compile(ApiRoot.class, code).run();

        Assertions.assertIterableEquals(List.of(11, 12), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}