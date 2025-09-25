package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.type.MethodReference;

import java.util.List;

public class BoundMethodNode extends BoundNode {

    public final NameExpressionNode syntaxNode;
    public final MethodReference method;

    public BoundMethodNode(NameExpressionNode node, MethodReference method) {
        this(node, method, node.getRange());
    }

    public BoundMethodNode(NameExpressionNode node, MethodReference method, TextRange range) {
        super(BoundNodeType.METHOD, range);
        this.syntaxNode = node;
        this.method = method;
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