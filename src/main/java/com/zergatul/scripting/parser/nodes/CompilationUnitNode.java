package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.collections.Lists;

import java.util.List;
import java.util.Objects;

public class CompilationUnitNode extends Node {

    public final List<StatementNode> statements;

    public CompilationUnitNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompilationUnitNode other) {
            return Objects.equals(other.statements, statements);
        } else {
            return false;
        }
    }
}