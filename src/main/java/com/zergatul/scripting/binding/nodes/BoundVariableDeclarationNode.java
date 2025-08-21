package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.VariableDeclarationNode;

import java.util.List;

public class BoundVariableDeclarationNode extends BoundStatementNode {

    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode expression;
    public final Token semicolon;

    public BoundVariableDeclarationNode(BoundNameExpressionNode name) {
        this(null, name, null, null, null);
    }

    public BoundVariableDeclarationNode(BoundNameExpressionNode name, BoundExpressionNode expression) {
        this(null, name, expression, null, null);
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, VariableDeclarationNode node) {
        this(type, name, expression, node.semicolon, node.getRange());
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        this(type, name, expression, null, range);
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, Token semicolon, TextRange range) {
        super(NodeType.VARIABLE_DECLARATION, range);
        this.type = type;
        this.name = name;
        this.expression = expression;
        this.semicolon = semicolon;
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
        return semicolon == null || semicolon.isMissing();
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