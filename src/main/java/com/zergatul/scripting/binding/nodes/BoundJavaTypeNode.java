package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.JavaTypeNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundJavaTypeNode extends BoundTypeNode {

    public final JavaTypeNode syntaxNode;

    public BoundJavaTypeNode(JavaTypeNode node, SType type) {
        this(node, type, node.getRange());
    }

    public BoundJavaTypeNode(JavaTypeNode node, SType type, TextRange range) {
        super(BoundNodeType.JAVA_TYPE, type, range);
        this.syntaxNode = node;
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