package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.TypeTestExpressionNode;
import com.zergatul.scripting.type.SBoolean;

import java.util.List;

public class BoundTypeTestExpressionNode extends BoundExpressionNode {

    public final TypeTestExpressionNode syntaxNode;
    public final BoundExpressionNode expression;
    public final BoundTypeNode type;

    public BoundTypeTestExpressionNode(TypeTestExpressionNode node, BoundExpressionNode expression, BoundTypeNode type) {
        this(node, expression, type, node.getRange());
    }

    public BoundTypeTestExpressionNode(TypeTestExpressionNode node, BoundExpressionNode expression, BoundTypeNode type, TextRange range) {
        super(BoundNodeType.TYPE_TEST_EXPRESSION, SBoolean.instance, range);
        this.syntaxNode = node;
        this.expression = expression;
        this.type = type;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
        type.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression, type);
    }
}