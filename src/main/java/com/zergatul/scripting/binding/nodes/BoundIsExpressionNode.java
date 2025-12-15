package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.IsExpressionNode;
import com.zergatul.scripting.type.SBoolean;

import java.util.List;

public class BoundIsExpressionNode extends BoundExpressionNode {

    public final IsExpressionNode syntaxNode;
    public final BoundExpressionNode expression;
    public final BoundPatternNode pattern;

    public BoundIsExpressionNode(IsExpressionNode node, BoundExpressionNode expression, BoundPatternNode pattern) {
        this(node, expression, pattern, node.getRange());
    }

    public BoundIsExpressionNode(IsExpressionNode node, BoundExpressionNode expression, BoundPatternNode pattern, TextRange range) {
        super(BoundNodeType.IS_EXPRESSION, SBoolean.instance, range);
        this.syntaxNode = node;
        this.expression = expression;
        this.pattern = pattern;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
        pattern.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression, pattern);
    }
}