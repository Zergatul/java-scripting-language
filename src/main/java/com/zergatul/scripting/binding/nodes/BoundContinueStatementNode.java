package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundContinueStatementNode extends BoundStatementNode {

    public BoundContinueStatementNode(TextRange range) {
        super(NodeType.CONTINUE_STATEMENT, range);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}