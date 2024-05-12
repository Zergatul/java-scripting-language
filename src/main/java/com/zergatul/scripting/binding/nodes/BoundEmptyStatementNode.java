package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundEmptyStatementNode extends BoundStatementNode {

    public BoundEmptyStatementNode(TextRange range) {
        super(NodeType.EMPTY_STATEMENT, range);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}