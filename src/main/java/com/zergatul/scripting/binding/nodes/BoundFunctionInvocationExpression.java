package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

public class BoundFunctionInvocationExpression extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final BoundArgumentsListNode arguments;

    public BoundFunctionInvocationExpression(BoundNameExpressionNode name, SType type, BoundArgumentsListNode arguments, TextRange range) {
        super(NodeType.FUNCTION_INVOCATION, type, range);
        this.name = name;
        this.arguments = arguments;
    }
}