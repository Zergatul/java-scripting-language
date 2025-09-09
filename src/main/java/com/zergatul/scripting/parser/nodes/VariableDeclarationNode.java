package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationNode extends StatementNode {

    public final TypeNode type;
    public final NameExpressionNode name;
    @Nullable public final Token equal;
    @Nullable public final ExpressionNode expression;
    public final Token semicolon;

    public VariableDeclarationNode(
            TypeNode type,
            NameExpressionNode name,
            @Nullable Token equal,
            @Nullable ExpressionNode expression,
            Token semicolon
    ) {
        super(ParserNodeType.VARIABLE_DECLARATION, TextRange.combine(type, semicolon));

        assert (equal != null) == (expression != null);

        this.type = type;
        this.name = name;
        this.equal = equal;
        this.expression = expression;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public boolean isOpen() {
        return (expression != null && expression.isMissing()) || name.isMissing();
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(type);
        nodes.add(name);
        if (equal != null) {
            nodes.add(equal);
        }
        if (expression != null) {
            nodes.add(expression);
        }
        nodes.add(semicolon);
        return nodes;
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
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new VariableDeclarationNode(type, name, equal, expression, semicolon);
    }
}