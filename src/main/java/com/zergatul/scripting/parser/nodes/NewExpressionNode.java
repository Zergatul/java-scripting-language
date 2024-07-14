package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

import java.util.List;

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
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        typeNode.accept(visitor);
        lengthExpression.accept(visitor);
        if (items != null) {
            for (ExpressionNode expression : items) {
                expression.accept(visitor);
            }
        }
    }
}