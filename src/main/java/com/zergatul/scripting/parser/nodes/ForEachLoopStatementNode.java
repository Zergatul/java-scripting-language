package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class ForEachLoopStatementNode extends StatementNode {
    public ForEachLoopStatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}