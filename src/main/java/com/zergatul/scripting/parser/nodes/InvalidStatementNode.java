package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidStatementNode extends StatementNode {

    public InvalidStatementNode(TextRange range) {
        super(ParserNodeType.INVALID_STATEMENT, range);
        if (!range.isEmpty()) {
            throw new InternalException();
        }
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new InvalidStatementNode(TextRange.combine(getRange(), semicolon.getRange()));
    }
}