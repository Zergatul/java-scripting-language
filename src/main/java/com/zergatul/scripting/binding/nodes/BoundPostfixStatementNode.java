package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.PostfixStatementNode;
import com.zergatul.scripting.type.operation.PostfixOperation;

import java.util.List;

public class BoundPostfixStatementNode extends BoundStatementNode {

    public final PostfixStatementNode syntaxNode;
    public final BoundExpressionNode expression;
    public final PostfixOperation operation;

    public BoundPostfixStatementNode(BoundNodeType nodeType, BoundExpressionNode expression, PostfixOperation operation) {
        this(SyntaxFactory.missingPostfixStatement(), nodeType, expression, operation, TextRange.MISSING);
    }

    public BoundPostfixStatementNode(PostfixStatementNode node, BoundNodeType nodeType, BoundExpressionNode expression, PostfixOperation operation) {
        this(node, nodeType, expression, operation, node.getRange());
    }

    public BoundPostfixStatementNode(PostfixStatementNode node, BoundNodeType nodeType, BoundExpressionNode expression, PostfixOperation operation, TextRange range) {
        super(nodeType, range);
        this.syntaxNode = node;
        this.expression = expression;
        this.operation = operation;
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