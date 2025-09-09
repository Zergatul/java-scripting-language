package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ForLoopStatementNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoundForLoopStatementNode extends BoundStatementNode {

    public final ForLoopStatementNode syntaxNode;
    @Nullable public final BoundStatementNode init;
    @Nullable public final BoundExpressionNode condition;
    @Nullable public final BoundStatementNode update;
    public final BoundStatementNode body;

    public BoundForLoopStatementNode(
            ForLoopStatementNode node,
            @Nullable BoundStatementNode init,
            @Nullable BoundExpressionNode condition,
            @Nullable BoundStatementNode update,
            BoundStatementNode body
    ) {
        this(node, init, condition, update, body, node.getRange());
    }

    public BoundForLoopStatementNode(
            ForLoopStatementNode node,
            @Nullable BoundStatementNode init,
            @Nullable BoundExpressionNode condition,
            @Nullable BoundStatementNode update,
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
        if (init != null) {
            init.accept(visitor);
        }
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
        List<BoundNode> nodes = new ArrayList<>();
        if (init != null) {
            nodes.add(init);
        }
        if (condition != null) {
            nodes.add(condition);
        }
        if (update != null) {
            nodes.add(update);
        }
        nodes.add(body);
        return nodes;
    }

    public BoundForLoopStatementNode withBody(BoundStatementNode body) {
        return new BoundForLoopStatementNode(syntaxNode, init, condition, update, body, getRange());
    }
}