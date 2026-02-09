package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ThrowStatementNode extends StatementNode {

    public final Token keyword;
    public final @Nullable ExpressionNode expression;
    public final @Nullable Token semicolon;

    public ThrowStatementNode(Token keyword, @Nullable ExpressionNode expression, @Nullable Token semicolon) {
        super(ParserNodeType.THROW_STATEMENT, TextRange.combineNullable(keyword, expression, semicolon));
        this.keyword = keyword;
        this.expression = expression;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> children = new ArrayList<>();
        children.add(keyword);
        if (expression != null) {
            children.add(expression);
        }
        if (semicolon != null) {
            children.add(semicolon);
        }
        return children;
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new ThrowStatementNode(keyword, expression, semicolon);
    }
}