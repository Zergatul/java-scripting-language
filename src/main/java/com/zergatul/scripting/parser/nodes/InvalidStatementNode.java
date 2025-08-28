package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidStatementNode extends StatementNode {

    public InvalidStatementNode(TextRange range) {
        super(NodeType.INVALID_STATEMENT, range);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvalidStatementNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}