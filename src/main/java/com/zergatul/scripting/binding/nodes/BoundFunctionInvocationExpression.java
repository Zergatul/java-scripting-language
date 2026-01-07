package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.InvocationExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundFunctionInvocationExpression extends BoundExpressionNode {

    public final InvocationExpressionNode syntaxNode;
    public final BoundFunctionNode functionNode;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundFunctionInvocationExpression(BoundFunctionNode functionNode, SType type, BoundArgumentsListNode arguments) {
        this(SyntaxFactory.missingInvocationExpression(), functionNode, type, arguments, List.of(), TextRange.MISSING);
    }

    public BoundFunctionInvocationExpression(
            InvocationExpressionNode node,
            BoundFunctionNode functionNode,
            SType type,
            BoundArgumentsListNode arguments,
            List<RefHolder> refVariables,
            TextRange range
    ) {
        super(BoundNodeType.FUNCTION_INVOCATION, type, range);
        this.syntaxNode = node;
        this.functionNode = functionNode;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        functionNode.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(functionNode, arguments);
    }
}