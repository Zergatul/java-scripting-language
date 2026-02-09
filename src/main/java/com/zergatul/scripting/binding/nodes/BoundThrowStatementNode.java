package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ThrowStatementNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundThrowStatementNode extends BoundStatementNode {

    public final ThrowStatementNode syntaxNode;
    public final @Nullable BoundExpressionNode expression;

    public BoundThrowStatementNode(ThrowStatementNode node, @Nullable BoundExpressionNode expression) {
        super(BoundNodeType.THROW_STATEMENT, node.getRange());
        this.syntaxNode = node;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return expression != null ?  List.of(expression) : List.of();
    }
}