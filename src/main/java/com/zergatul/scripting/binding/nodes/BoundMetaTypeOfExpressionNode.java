package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeOfExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;

    public BoundMetaTypeOfExpressionNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.META_TYPE_OF_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return this;
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}