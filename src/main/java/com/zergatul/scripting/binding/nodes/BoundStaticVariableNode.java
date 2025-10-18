package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.StaticVariableNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundStaticVariableNode extends BoundCompilationUnitMemberNode {

    public final StaticVariableNode syntaxNode;
    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    @Nullable public final BoundExpressionNode expression;

    public BoundStaticVariableNode(
            StaticVariableNode node,
            BoundTypeNode type,
            BoundNameExpressionNode name,
            @Nullable BoundExpressionNode expression
    ) {
        this(node, type, name, expression, node.getRange());
    }

    public BoundStaticVariableNode(
            StaticVariableNode node,
            BoundTypeNode type,
            BoundNameExpressionNode name,
            @Nullable BoundExpressionNode expression,
            TextRange range
    ) {
        super(BoundNodeType.STATIC_VARIABLE, range);
        this.syntaxNode = node;
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean isOpen() {
        return (expression != null && expression.isMissing()) || name.isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        if (expression != null) {
            return List.of(type, name, expression);
        } else {
            return List.of(type, name);
        }
    }
}