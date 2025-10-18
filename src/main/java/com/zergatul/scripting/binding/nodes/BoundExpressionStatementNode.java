package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.ExpressionStatementNode;

import java.util.List;

public class BoundExpressionStatementNode extends BoundStatementNode {

    public final ExpressionStatementNode syntaxNode;
    public final BoundExpressionNode expression;

    public BoundExpressionStatementNode(BoundExpressionNode expression) {
        this(SyntaxFactory.missingExpressionStatement(), expression, TextRange.MISSING);
    }

    public BoundExpressionStatementNode(ExpressionStatementNode node, BoundExpressionNode expression) {
        this(node, expression, node.getRange());
    }

    public BoundExpressionStatementNode(ExpressionStatementNode node, BoundExpressionNode expression, TextRange range) {
        super(BoundNodeType.EXPRESSION_STATEMENT, range);
        this.syntaxNode = node;
        this.expression = expression;
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