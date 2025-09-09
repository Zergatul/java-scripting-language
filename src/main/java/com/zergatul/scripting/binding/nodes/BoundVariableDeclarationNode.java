package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.VariableDeclarationNode;

import java.util.List;

public class BoundVariableDeclarationNode extends BoundStatementNode {

    public final VariableDeclarationNode syntaxNode;
    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode expression;

    public BoundVariableDeclarationNode(BoundNameExpressionNode name) {
        this(null, null, name, null, null);
    }

    public BoundVariableDeclarationNode(BoundNameExpressionNode name, BoundExpressionNode expression) {
        this(null, null, name, expression, null);
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        this(null, type, name, expression, range);
    }

    public BoundVariableDeclarationNode(VariableDeclarationNode node, BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression) {
        this(node, type, name, expression, node.getRange());
    }

    public BoundVariableDeclarationNode(VariableDeclarationNode node, BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        super(BoundNodeType.VARIABLE_DECLARATION, range);
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
    public boolean isOpen() {
        return syntaxNode.semicolon == null || syntaxNode.semicolon.isMissing();
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