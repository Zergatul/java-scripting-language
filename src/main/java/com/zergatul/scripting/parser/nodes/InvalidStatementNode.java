package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class InvalidStatementNode extends StatementNode {
    public InvalidStatementNode(TextRange range) {
        super(range);
    }
}