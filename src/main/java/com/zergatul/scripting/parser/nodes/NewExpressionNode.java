package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class NewExpressionNode extends ExpressionNode {

    public final TypeNode typeNode;
    public final ExpressionNode lengthExpression;
    public final List<ExpressionNode> items;

    public NewExpressionNode(TypeNode typeNode, ExpressionNode lengthExpression, List<ExpressionNode> items, TextRange range) {
        super(NodeType.NEW_EXPRESSION, range);
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
        this.items = items;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        if (lengthExpression != null) {
            lengthExpression.accept(visitor);
        }
        if (items != null) {
            for (ExpressionNode expression : items) {
                expression.accept(visitor);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NewExpressionNode other) {
            return  other.typeNode.equals(typeNode) &&
                    Objects.equals(other.lengthExpression, lengthExpression) &&
                    Objects.equals(other.items, items) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}