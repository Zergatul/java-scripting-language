package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundInvalidStatementNode extends BoundStatementNode {

    public BoundInvalidStatementNode(TextRange range) {
        super(NodeType.INVALID_STATEMENT, range);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundInvalidStatementNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}