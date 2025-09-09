package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MetaTypeOfExpressionNode;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeOfExpressionNode extends BoundExpressionNode {

    public final MetaTypeOfExpressionNode syntaxNode;
    public final BoundExpressionNode expression;

    public BoundMetaTypeOfExpressionNode(MetaTypeOfExpressionNode node, BoundExpressionNode expression) {
        this(node, expression, node.getRange());
    }

    public BoundMetaTypeOfExpressionNode(MetaTypeOfExpressionNode node, BoundExpressionNode expression, TextRange range) {
        super(BoundNodeType.META_TYPE_OF_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
        this.syntaxNode = node;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}