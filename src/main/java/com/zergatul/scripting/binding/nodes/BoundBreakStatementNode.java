package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundBreakStatementNode extends BoundStatementNode {

    public BoundBreakStatementNode(TextRange range) {
        super(NodeType.BREAK_STATEMENT, range);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}