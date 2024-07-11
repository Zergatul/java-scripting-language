package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundMethodInvocationExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode objectReference;
    public final BoundMethodNode method;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundMethodInvocationExpressionNode(BoundExpressionNode objectReference, BoundMethodNode method, BoundArgumentsListNode arguments, List<RefHolder> refVariables, TextRange range) {
        super(NodeType.METHOD_INVOCATION_EXPRESSION, method.method.getReturn(), range);
        this.objectReference = objectReference;
        this.method = method;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    @Override
    public boolean isAsync() {
        for (BoundExpressionNode expression : arguments.arguments) {
            if (expression.isAsync()) {
                return true;
            }
        }
        return objectReference.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(objectReference, method, arguments);
    }
}