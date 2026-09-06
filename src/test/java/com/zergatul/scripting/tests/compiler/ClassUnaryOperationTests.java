package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "class Vec2 {\n" +
                "    float x;\n" +
                "    float y;\n" +
                "\n" +
                "    constructor(float x, float y) {\n" +
                "        this.x = x;\n" +
                "        this.y = y;\n" +
                "    }\n" +
                "\n" +
                "    override string toString() {\n" +
                "        return \"(\" + x + \"; \" + y + \")\";\n" +
                "    }\n" +
                "\n" +
                "    operator [+] Vec2(Vec2 vec) => vec;\n" +
                "\n" +
                "    operator [+] Vec2(Vec2 left, Vec2 right) {\n" +
                "        return new Vec2(left.x + right.x, left.y + right.y);\n" +
                "    }\n" +
                "\n" +
                "    operator [-] Vec2(Vec2 vec) => new Vec2(-vec.x, -vec.y);\n" +
                "\n" +
                "    operator [-] Vec2(Vec2 left, Vec2 right) {\n" +
                "        return new Vec2(left.x - right.x, left.y - right.y);\n" +
                "    }\n" +
                "\n" +
                "    operator [!] Vec2(Vec2 vec) => new Vec2(vec.y, vec.x);\n" +
                "}\n" +
                "\n" +
                "void log(Vec2 v) => stringStorage.add(v.toString());\n" +
                "\n" +
                "let v = new Vec2(1, 2);\n" +
                "log(+v);\n" +
                "log(-v);\n" +
                "log(!v);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of(
                        "(1.0; 2.0)",
                        "(-1.0; -2.0)",
                        "(2.0; 1.0)"));
    }

    @Test
    public void doubleOverloadTest() {
        String code =
                "class MyClass {\n" +
                "    operator [!] boolean(MyClass instance) => true;\n" +
                "    operator [!] int(MyClass instance) => 1;\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(BinderErrors.UnaryOperationAlreadyDeclared, new SingleLineTextRange(3, 21, 88, 18))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
    }
}