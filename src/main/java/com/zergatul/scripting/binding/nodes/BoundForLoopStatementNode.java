package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ForLoopStatementNode;

import java.util.List;

public class BoundForLoopStatementNode extends BoundStatementNode {

    public final ForLoopStatementNode syntaxNode;
    public final BoundStatementNode init;
    public final BoundExpressionNode condition;
    public final BoundStatementNode update;
    public final BoundStatementNode body;

    public BoundForLoopStatementNode(
            ForLoopStatementNode node,
            BoundStatementNode init,
            BoundExpressionNode condition,
            BoundStatementNode update,
            BoundStatementNode body
    ) {
        this(node, init, condition, update, body, node.getRange());
    }

    public BoundForLoopStatementNode(
            ForLoopStatementNode node,
            BoundStatementNode init,
            BoundExpressionNode condition,
            BoundStatementNode update,
            BoundStatementNode body,
            TextRange range
    ) {
        super(BoundNodeType.FOR_LOOP_STATEMENT, range);
        this.syntaxNode = node;
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        init.accept(visitor);
        if (condition != null) {
            condition.accept(visitor);
        }
        if (update != null) {
            update.accept(visitor);
        }
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        if (condition != null) {
            return List.of(init, condition, update, body);
        } else {
            return List.of(init, update, body);
        }
    }

    public BoundForLoopStatementNode withBody(BoundStatementNode body) {
        return new BoundForLoopStatementNode(syntaxNode, init, condition, update, body, getRange());
    }
}