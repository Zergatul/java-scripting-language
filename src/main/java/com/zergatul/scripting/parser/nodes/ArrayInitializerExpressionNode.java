package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class ArrayInitializerExpressionNode extends ExpressionNode {

    public final TypeNode typeNode;
    public final List<ExpressionNode> items;

    public ArrayInitializerExpressionNode(TypeNode typeNode, List<ExpressionNode> items, TextRange range) {
        super(NodeType.ARRAY_INITIALIZER_EXPRESSION, range);
        this.typeNode = typeNode;
        this.items = items;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode expression : items) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayInitializerExpressionNode other) {
            return  other.typeNode.equals(typeNode) &&
                    Objects.equals(other.items, items) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}