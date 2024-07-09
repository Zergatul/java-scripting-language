package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BreakStatementNode extends StatementNode {

    public BreakStatementNode(TextRange range) {
        super(NodeType.BREAK_STATEMENT, range);
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}