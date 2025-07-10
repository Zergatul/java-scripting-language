package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidMetaExpressionNode extends ExpressionNode {

    public InvalidMetaExpressionNode(TextRange range) {
        super(NodeType.META_INVALID_EXPRESSION, range);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        throw new RuntimeException();
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        throw new RuntimeException();
    }
}
