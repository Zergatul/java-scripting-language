package com.zergatul.scripting.parser.nodes;

import java.util.List;
import java.util.Objects;

public class InvocationExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final List<ExpressionNode> arguments;

    public InvocationExpressionNode(ExpressionNode callee, List<ExpressionNode> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvocationExpressionNode other) {
            return other.callee.equals(callee) && Objects.equals(other.arguments, arguments);
        } else {
            return false;
        }
    }
}