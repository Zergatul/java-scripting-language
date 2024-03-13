package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.MethodReference;

public class BoundInvocationExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundArgumentsListNode arguments;
    public final MethodReference method;

    public BoundInvocationExpressionNode(BoundExpressionNode callee, BoundArgumentsListNode arguments, MethodReference method, TextRange range) {
        super(NodeType.INVOCATION_EXPRESSION, method.getReturn(), range);
        this.callee = callee;
        this.arguments = arguments;
        this.method = method;
    }
}