package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class InvalidStatementNode extends StatementNode {
    public InvalidStatementNode(TextRange range) {
        super(NodeType.INVALID_STATEMENT, range);
    }
}