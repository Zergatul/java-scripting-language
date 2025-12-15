package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ConstantPatternNode;

import java.util.List;

public class BoundConstantPatternNode extends BoundPatternNode {

    public final ConstantPatternNode syntaxNode;
    public final BoundExpressionNode expression;

    public BoundConstantPatternNode(ConstantPatternNode node, BoundExpressionNode expression, TextRange range) {
        super(BoundNodeType.CONSTANT_PATTERN, range);
        this.syntaxNode = node;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}