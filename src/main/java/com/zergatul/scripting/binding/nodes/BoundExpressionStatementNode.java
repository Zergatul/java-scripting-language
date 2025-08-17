package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundExpressionStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundExpressionStatementNode(BoundExpressionNode expression) {
        this(expression, null);
    }

    public BoundExpressionStatementNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.EXPRESSION_STATEMENT, range);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundExpressionStatementNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }

    public BoundExpressionStatementNode update(BoundExpressionNode expression) {
        if (this.expression != expression) {
            return new BoundExpressionStatementNode(expression, getRange());
        } else {
            return this;
        }
    }
}