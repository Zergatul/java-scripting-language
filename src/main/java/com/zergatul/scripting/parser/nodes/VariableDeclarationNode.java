package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class VariableDeclarationNode extends StatementNode {

    public final TypeNode type;
    public final NameExpressionNode name;
    public final ExpressionNode expression;

    public VariableDeclarationNode(TypeNode type, NameExpressionNode name, TextRange range) {
        this(type, name, null, range);
    }

    public VariableDeclarationNode(TypeNode type, NameExpressionNode name, ExpressionNode expression, TextRange range) {
        super(NodeType.VARIABLE_DECLARATION, range);
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableDeclarationNode other) {
            return  other.type.equals(type) &&
                    other.name.equals(name) &&
                    Objects.equals(other.expression, expression) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }

    @Override
    public StatementNode prepend(Token token) {
        return new VariableDeclarationNode(type, name, expression, TextRange.combine(token.getRange(), getRange()));
    }

    @Override
    public StatementNode append(Token token) {
        return new VariableDeclarationNode(type, name, expression, TextRange.combine(getRange(), token.getRange()));
    }
}