package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;

import java.util.List;

public class BoundUnresolvedMethodNode extends BoundNode {

    public final NameExpressionNode syntaxNode;
    public final String name;

    public BoundUnresolvedMethodNode(NameExpressionNode node) {
        this(node, node.value, node.getRange());
    }

    public BoundUnresolvedMethodNode(NameExpressionNode node, String name, TextRange range) {
        super(BoundNodeType.UNRESOLVED_METHOD, range);
        this.syntaxNode = node;
        this.name = name;
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