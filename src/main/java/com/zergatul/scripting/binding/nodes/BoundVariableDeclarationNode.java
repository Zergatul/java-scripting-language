package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundVariableDeclarationNode extends BoundStatementNode {

    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode expression;

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        super(NodeType.VARIABLE_DECLARATION, range);
        this.type = type;
        this.name = name;
        this.expression = expression;
    }
}