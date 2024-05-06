package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundFunctionInvocationExpression extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundFunctionInvocationExpression(BoundNameExpressionNode name, SType type, BoundArgumentsListNode arguments, List<RefHolder> refVariables, TextRange range) {
        super(NodeType.FUNCTION_INVOCATION, type, range);
        this.name = name;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }
}