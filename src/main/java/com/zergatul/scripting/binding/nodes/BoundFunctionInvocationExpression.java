package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.RefHolder;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundFunctionInvocationExpression extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final BoundArgumentsListNode arguments;
    public final List<RefHolder> refVariables;

    public BoundFunctionInvocationExpression(BoundNameExpressionNode name, SType type, BoundArgumentsListNode arguments) {
        this(name, type, arguments, List.of(), null);
    }

    public BoundFunctionInvocationExpression(BoundNameExpressionNode name, SType type, BoundArgumentsListNode arguments, List<RefHolder> refVariables, TextRange range) {
        super(NodeType.FUNCTION_INVOCATION, type, range);
        this.name = name;
        this.arguments = arguments;
        this.refVariables = refVariables;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(name, arguments);
    }
}