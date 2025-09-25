package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.InvalidStatementNode;

import java.util.List;

public class BoundInvalidStatementNode extends BoundStatementNode {

    public final InvalidStatementNode syntaxNode;

    public BoundInvalidStatementNode(InvalidStatementNode node) {
        super(BoundNodeType.INVALID_STATEMENT, node.getRange());
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public boolean isOpen() {
        return isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}