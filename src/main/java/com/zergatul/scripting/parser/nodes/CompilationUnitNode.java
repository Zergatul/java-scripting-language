package com.zergatul.scripting.parser.nodes;


import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class CompilationUnitNode extends Node {

    public final StaticVariablesListNode variables;
    public final FunctionsListNode functions;
    public final StatementsListNode statements;

    public CompilationUnitNode(StaticVariablesListNode variables, FunctionsListNode functions, StatementsListNode statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.variables = variables;
        this.functions = functions;
        this.statements = statements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompilationUnitNode other) {
            return  other.variables.equals(variables) &&
                    other.functions.equals(functions) &&
                    other.statements.equals(statements) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}