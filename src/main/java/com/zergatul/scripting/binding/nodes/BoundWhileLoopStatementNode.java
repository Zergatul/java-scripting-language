package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundWhileLoopStatementNode extends BoundStatementNode {

    public final BoundExpressionNode condition;
    public final BoundStatementNode body;

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body) {
        this(condition, body, null);
    }

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body, TextRange range) {
        super(NodeType.WHILE_LOOP_STATEMENT, range);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, body);
    }
}