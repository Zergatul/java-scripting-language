package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MetaCastExpressionNode;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaCastExpressionNode extends BoundExpressionNode {

    public final MetaCastExpressionNode syntaxNode;
    public final BoundExpressionNode expression;
    public final BoundTypeNode type;

    public BoundMetaCastExpressionNode(MetaCastExpressionNode node, BoundExpressionNode expression, BoundTypeNode type) {
        this(node, expression, type, node.getRange());
    }

    public BoundMetaCastExpressionNode(MetaCastExpressionNode node, BoundExpressionNode expression, BoundTypeNode type, TextRange range) {
        super(BoundNodeType.META_CAST_EXPRESSION, type.type, range);
        this.syntaxNode = node;
        this.expression = expression;
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
        return List.of(expression, type);
    }
}