package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class PostfixStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public PostfixStatementNode(NodeType nodeType, ExpressionNode expression, TextRange range) {
        super(nodeType, range);
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
        if (obj instanceof PostfixStatementNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new PostfixStatementNode(getNodeType(), expression, TextRange.combine(getRange(), semicolon.getRange()));
    }
}