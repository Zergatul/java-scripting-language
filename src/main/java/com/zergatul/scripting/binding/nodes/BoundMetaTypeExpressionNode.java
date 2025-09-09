package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MetaTypeExpressionNode;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeExpressionNode extends BoundExpressionNode {

    public final MetaTypeExpressionNode syntaxNode;
    public final BoundTypeNode type;

    public BoundMetaTypeExpressionNode(MetaTypeExpressionNode node, BoundTypeNode type) {
        this(node, type, node.getRange());
    }

    public BoundMetaTypeExpressionNode(MetaTypeExpressionNode node, BoundTypeNode type, TextRange range) {
        super(BoundNodeType.META_TYPE_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
        this.syntaxNode = node;
        this.type = type;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(type);
    }
}