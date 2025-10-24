package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.nodes.BaseExpressionNode;
import com.zergatul.scripting.parser.nodes.InvocationExpressionNode;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;

import java.util.List;

public class BoundBaseMethodInvocationExpressionNode extends BoundExpressionNode {

    public final InvocationExpressionNode syntaxNode;
    public final BoundMethodNode method;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundBaseMethodInvocationExpressionNode(
            InvocationExpressionNode node,
            BoundMethodNode method,
            BoundArgumentsListNode arguments,
            List<RefHolder> refVariables
    ) {
        this(node, method, arguments, refVariables, node.getRange());
    }

    public BoundBaseMethodInvocationExpressionNode(
            InvocationExpressionNode node,
            BoundMethodNode method,
            BoundArgumentsListNode arguments,
            List<RefHolder> refVariables,
            TextRange range
    ) {
        super(BoundNodeType.BASE_METHOD_INVOCATION_EXPRESSION, method.method.getReturn(), range);
        this.syntaxNode = node;
        this.method = method;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        method.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(method, arguments);
    }

    public BaseExpressionNode getBaseExpressionSyntaxNode() {
        return (BaseExpressionNode) ((MemberAccessExpressionNode) syntaxNode.callee).callee;
    }
}
