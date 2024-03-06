package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class LiteralExpressionNode extends ExpressionNode {
    protected LiteralExpressionNode(TextRange range) {
        super(range);
    }
}