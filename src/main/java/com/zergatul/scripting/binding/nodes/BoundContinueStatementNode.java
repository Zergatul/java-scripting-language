package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundContinueStatementNode extends BoundStatementNode {

    public BoundContinueStatementNode(TextRange range) {
        super(BoundNodeType.CONTINUE_STATEMENT, range);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}