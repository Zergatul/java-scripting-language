package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundInvalidStatementNode extends BoundStatementNode {

    public BoundInvalidStatementNode(TextRange range) {
        super(NodeType.INVALID_STATEMENT, range);
    }
}