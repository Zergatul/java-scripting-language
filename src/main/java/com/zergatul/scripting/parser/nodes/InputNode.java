package com.zergatul.scripting.parser.nodes;

import java.util.List;

public class InputNode {

    public final List<StatementNode> statements;

    public InputNode(List<StatementNode> statements) {
        this.statements = statements;
    }
}