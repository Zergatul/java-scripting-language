package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class BoundReturnStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundReturnStatementNode(BoundExpressionNode expression) {
        this(expression, null);
    }

    public BoundReturnStatementNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.RETURN_STATEMENT, range);
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
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return expression == null ? List.of() : List.of(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundReturnStatementNode other) {
            return Objects.equals(other.expression, expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }

    public BoundReturnStatementNode update(BoundExpressionNode expression) {
        if (this.expression != expression) {
            return new BoundReturnStatementNode(expression, getRange());
        } else {
            return this;
        }
    }
}