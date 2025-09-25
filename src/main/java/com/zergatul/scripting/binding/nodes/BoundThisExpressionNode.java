package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ThisExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundThisExpressionNode extends BoundExpressionNode {

    public final ThisExpressionNode syntaxNode;

    public BoundThisExpressionNode(ThisExpressionNode node, SType type) {
        this(node, type, node.getRange());
    }

    public BoundThisExpressionNode(ThisExpressionNode node, SType type, TextRange range) {
        super(BoundNodeType.THIS_EXPRESSION, type, range);
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
