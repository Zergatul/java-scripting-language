package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class RefArgumentExpressionNode extends ExpressionNode {

    public final NameExpressionNode name;

    public RefArgumentExpressionNode(NameExpressionNode name, TextRange range) {
        super(NodeType.REF_ARGUMENT_EXPRESSION, range);
        this.name = name;
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}