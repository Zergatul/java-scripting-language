package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class RefExpressionNode extends ExpressionNode {

    public final NameExpressionNode name;

    public RefExpressionNode(NameExpressionNode name, TextRange range) {
        super(NodeType.REF_EXPRESSION, range);
        this.name = name;
    }
}