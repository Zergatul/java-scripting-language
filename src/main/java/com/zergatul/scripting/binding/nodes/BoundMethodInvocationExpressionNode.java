package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.InvocationExpressionNode;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;

import java.util.List;

public class BoundMethodInvocationExpressionNode extends BoundExpressionNode {

    public final InvocationExpressionNode syntaxNode;
    public final BoundExpressionNode objectReference;
    public final BoundMethodNode method;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundMethodInvocationExpressionNode(
            InvocationExpressionNode node,
            BoundExpressionNode objectReference,
            BoundMethodNode method,
            BoundArgumentsListNode arguments,
            List<RefHolder> refVariables
    ) {
        this(node, objectReference, method, arguments, refVariables, node.getRange());
    }

    public BoundMethodInvocationExpressionNode(
            InvocationExpressionNode node,
            BoundExpressionNode objectReference,
            BoundMethodNode method,
            BoundArgumentsListNode arguments,
            List<RefHolder> refVariables,
            TextRange range
    ) {
        super(BoundNodeType.METHOD_INVOCATION_EXPRESSION, method.method.getReturn(), range);
        this.syntaxNode = node;
        this.objectReference = objectReference;
        this.method = method;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    public Token getDotToken() {
        return ((MemberAccessExpressionNode) syntaxNode.callee).dot;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        objectReference.accept(visitor);
        method.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(objectReference, method, arguments);
    }
}