package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode type;

    public BoundMetaTypeExpressionNode(BoundTypeNode type, TextRange range) {
        super(NodeType.META_TYPE_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
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