package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class ContinueStatementNode extends StatementNode {

    public ContinueStatementNode(TextRange range) {
        super(NodeType.CONTINUE_STATEMENT, range);
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}