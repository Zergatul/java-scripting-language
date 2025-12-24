package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ClassUnaryOperationTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                class Vec2 {
                    float x;
                    float y;
                
                    constructor(float x, float y) {
                        this.x = x;
                        this.y = y;
                    }
                
                    override string toString() {
                        return "(" + x + "; " + y + ")";
                    }
                
                    operator [+] Vec2(Vec2 vec) => vec;
                
                    operator [+] Vec2(Vec2 left, Vec2 right) {
                        return new Vec2(left.x + right.x, left.y + right.y);
                    }
                
                    operator [-] Vec2(Vec2 vec) => new Vec2(-vec.x, -vec.y);
                
                    operator [-] Vec2(Vec2 left, Vec2 right) {
                        return new Vec2(left.x - right.x, left.y - right.y);
                    }
                
                    operator [!] Vec2(Vec2 vec) => new Vec2(vec.y, vec.x);
                }
                
                void log(Vec2 v) => stringStorage.add(v.toString());
                
                let v = new Vec2(1, 2);
                log(+v);
                log(-v);
                log(!v);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of(
                        "(1.0; 2.0)",
                        "(-1.0; -2.0)",
                        "(2.0; 1.0)"));
    }

    @Test
    public void doubleOverloadTest() {
        String code = """
                class MyClass {
                    operator [!] boolean(MyClass instance) => true;
                    operator [!] int(MyClass instance) => 1;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.UnaryOperationAlreadyDeclared, new SingleLineTextRange(3, 21, 88, 18))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
    }
}