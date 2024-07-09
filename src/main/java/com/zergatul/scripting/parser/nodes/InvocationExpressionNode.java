package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class InvocationExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final ArgumentsListNode arguments;

    public InvocationExpressionNode(ExpressionNode callee, ArgumentsListNode arguments, TextRange range) {
        super(NodeType.INVOCATION_EXPRESSION, range);
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public boolean isAsync() {
        return callee.isAsync() || arguments.arguments.stream().anyMatch(ExpressionNode::isAsync);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvocationExpressionNode other) {
            return  other.callee.equals(callee) &&
                    other.arguments.equals(arguments) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}