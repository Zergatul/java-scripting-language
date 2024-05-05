package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.MethodReference;

import java.util.List;

public class BoundMethodInvocationExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode objectReference;
    public final MethodReference method;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundMethodInvocationExpressionNode(BoundExpressionNode objectReference, MethodReference method, BoundArgumentsListNode arguments, List<RefHolder> refVariables, TextRange range) {
        super(NodeType.METHOD_INVOCATION_EXPRESSION, method.getReturn(), range);
        this.objectReference = objectReference;
        this.method = method;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }
}