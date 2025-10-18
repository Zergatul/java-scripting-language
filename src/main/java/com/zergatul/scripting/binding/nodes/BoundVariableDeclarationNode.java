package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.VariableDeclarationNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoundVariableDeclarationNode extends BoundStatementNode {

    public final VariableDeclarationNode syntaxNode;
    @Nullable public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    @Nullable public final BoundExpressionNode expression;

    public BoundVariableDeclarationNode(BoundNameExpressionNode name) {
        this(SyntaxFactory.missingVariableDeclaration(), null, name, null, TextRange.MISSING);
    }

    public BoundVariableDeclarationNode(BoundNameExpressionNode name, BoundExpressionNode expression) {
        this(SyntaxFactory.missingVariableDeclaration(), null, name, expression, TextRange.MISSING);
    }

    public BoundVariableDeclarationNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        this(SyntaxFactory.missingVariableDeclaration(), type, name, expression, range);
    }

    public BoundVariableDeclarationNode(
            VariableDeclarationNode node,
            BoundTypeNode type,
            BoundNameExpressionNode name,
            @Nullable BoundExpressionNode expression
    ) {
        this(node, type, name, expression, node.getRange());
    }

    public BoundVariableDeclarationNode(
            VariableDeclarationNode node,
            @Nullable BoundTypeNode type,
            BoundNameExpressionNode name,
            @Nullable BoundExpressionNode expression,
            TextRange range
    ) {
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
        return syntaxNode.semicolon.isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> children = new ArrayList<>();
        if (type != null) {
            children.add(type);
        }
        children.add(name);
        if (expression != null) {
            children.add(expression);
        }
        return children;
    }
}