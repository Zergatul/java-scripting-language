package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.InvalidMetaExpressionNode;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidMetaExpressionNode extends BoundExpressionNode {

    public final InvalidMetaExpressionNode syntaxNode;

    public BoundInvalidMetaExpressionNode(InvalidMetaExpressionNode node) {
        this(node, node.getRange());
    }

    public BoundInvalidMetaExpressionNode(InvalidMetaExpressionNode node, TextRange range) {
        super(BoundNodeType.META_INVALID_EXPRESSION, SUnknown.instance, range);
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}