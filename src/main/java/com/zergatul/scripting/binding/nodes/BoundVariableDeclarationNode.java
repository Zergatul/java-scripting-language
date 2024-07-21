package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundVariableDeclarationNode extends BoundStatementNode {

    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode expression;

    public BoundVariableDeclarationNode(BoundNameExpressionNode name) {
        this(null, name, null, null);
    }

    public BoundVariableDeclarationNode(BoundNameExpressionNode name, BoundExpressionNode expression) {
        this(null, name, expression, null);
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        super(NodeType.VARIABLE_DECLARATION, range);
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
        // type can be null for generator tree
        if (type != null) {
            type.accept(visitor);
        }
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(type, name, expression);
    }
}