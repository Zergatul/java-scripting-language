package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;

public class InvalidStatementNode extends StatementNode {

    public InvalidStatementNode(TextRange range) {
        super(NodeType.INVALID_STATEMENT, range);
    }

    @Override
    public StatementNode append(Token token) {
        return new InvalidStatementNode(TextRange.combine(getRange(), token.getRange()));
    }
}