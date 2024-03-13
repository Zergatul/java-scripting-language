package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class EmptyStatementNode extends StatementNode {
    public EmptyStatementNode(TextRange range) {
        super(NodeType.EMPTY_STATEMENT, range);
    }
}