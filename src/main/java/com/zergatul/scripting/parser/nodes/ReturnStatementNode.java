package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ReturnStatementNode extends StatementNode {

    public final Token keyword;
    @Nullable
    public final ExpressionNode expression;
    public final Token semicolon;

    public ReturnStatementNode(
            Token keyword,
            @Nullable ExpressionNode expression,
            Token semicolon
    ) {
        super(ParserNodeType.RETURN_STATEMENT, TextRange.combine(keyword, semicolon));
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
        if (expression != null) {
            return List.of(keyword, expression, semicolon);
        } else {
            return List.of(keyword, semicolon);
        }
    }
}