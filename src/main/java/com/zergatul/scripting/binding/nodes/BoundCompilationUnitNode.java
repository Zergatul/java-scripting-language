package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundCompilationUnitNode extends BoundNode {

    public final List<BoundVariableDeclarationNode> variables;
    public final List<BoundFunctionNode> functions;
    public final List<BoundStatementNode> statements;

    public BoundCompilationUnitNode(List<BoundVariableDeclarationNode> variables, List<BoundFunctionNode> functions, List<BoundStatementNode> statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.variables = variables;
        this.functions = functions;
        this.statements = statements;
    }
}