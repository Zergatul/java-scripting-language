package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ExpressionStatementNode;

import java.util.List;

public class BoundExpressionStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;
    public final Token semicolon;

    public BoundExpressionStatementNode(BoundExpressionNode expression) {
        this(expression, null, null);
    }

    public BoundExpressionStatementNode(ExpressionStatementNode node, BoundExpressionNode expression) {
        this(expression, node.semicolon, node.getRange());
    }

    public BoundExpressionStatementNode(BoundExpressionNode expression, Token semicolon, TextRange range) {
        super(BoundNodeType.EXPRESSION_STATEMENT, range);
        this.expression = expression;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}