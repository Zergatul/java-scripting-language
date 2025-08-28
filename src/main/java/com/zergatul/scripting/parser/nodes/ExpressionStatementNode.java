package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ExpressionStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression, TextRange range) {
        super(NodeType.EXPRESSION_STATEMENT, range);
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpressionStatementNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new ExpressionStatementNode(expression, TextRange.combine(getRange(), semicolon.getRange()));
    }
}