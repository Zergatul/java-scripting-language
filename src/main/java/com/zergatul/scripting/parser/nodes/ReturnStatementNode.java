package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class ReturnStatementNode extends StatementNode {

    public final Token keyword;
    public final ExpressionNode expression;

    public ReturnStatementNode(Token keyword, ExpressionNode expression, TextRange range) {
        super(ParserNodeType.RETURN_STATEMENT, range);
        this.keyword = keyword;
        this.expression = expression;
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
    public boolean equals(Object obj) {
        if (obj instanceof ReturnStatementNode other) {
            return  other.keyword.equals(keyword) &&
                    Objects.equals(other.expression, expression) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}