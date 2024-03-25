package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundBreakStatementNode extends BoundStatementNode {

    public BoundBreakStatementNode(TextRange range) {
        super(NodeType.BREAK_STATEMENT, range);
    }
}