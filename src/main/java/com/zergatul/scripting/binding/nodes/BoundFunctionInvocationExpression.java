package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundFunctionInvocationExpression extends BoundExpressionNode {

    public final BoundFunctionReferenceNode functionReferenceNode;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundFunctionInvocationExpression(BoundFunctionReferenceNode functionReferenceNode, SType type, BoundArgumentsListNode arguments) {
        this(functionReferenceNode, type, arguments, List.of(), null);
    }

    public BoundFunctionInvocationExpression(BoundFunctionReferenceNode functionReferenceNode, SType type, BoundArgumentsListNode arguments, List<RefHolder> refVariables, TextRange range) {
        super(NodeType.FUNCTION_INVOCATION, type, range);
        this.functionReferenceNode = functionReferenceNode;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        functionReferenceNode.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(functionReferenceNode, arguments);
    }
}