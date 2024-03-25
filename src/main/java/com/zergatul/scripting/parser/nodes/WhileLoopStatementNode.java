package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class WhileLoopStatementNode extends StatementNode {
    public WhileLoopStatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}