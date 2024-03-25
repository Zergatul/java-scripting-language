package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundEmptyStatementNode extends BoundStatementNode {

    public BoundEmptyStatementNode(TextRange range) {
        super(NodeType.EMPTY_STATEMENT, range);
    }
}