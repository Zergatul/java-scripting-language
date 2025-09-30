package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.EndOfFileToken;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.CompilationUnitMembersListNode;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;
import com.zergatul.scripting.parser.nodes.StatementsListNode;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassTests extends ParserTestBase {

    @Test
    public void unfinishedMemberTest() {
        ParserOutput result = parse("""
                class Region {
                    void
                }
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new EndOfFileToken(new SingleLineTextRange(1, 1, 0, 0))),
                result.unit());
    }
}