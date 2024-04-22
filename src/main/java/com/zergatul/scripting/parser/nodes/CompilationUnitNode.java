package com.zergatul.scripting.parser.nodes;


import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class CompilationUnitNode extends Node {

    public final List<VariableDeclarationNode> variables;
    public final List<FunctionNode> functions;
    public final List<StatementNode> statements;

    public CompilationUnitNode(List<StatementNode> statements, TextRange range) {
        this(List.of(), List.of(), statements, range);
    }

    public CompilationUnitNode(List<VariableDeclarationNode> variables, List<FunctionNode> functions, List<StatementNode> statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.variables = variables;
        this.functions = functions;
        this.statements = statements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompilationUnitNode other) {
            return  Objects.equals(other.variables, variables) &&
                    Objects.equals(other.functions, functions) &&
                    Objects.equals(other.statements, statements) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}