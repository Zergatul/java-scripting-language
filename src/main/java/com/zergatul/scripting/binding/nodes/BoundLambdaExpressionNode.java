package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.compiler.CapturedAsyncStateMachineFieldVariable;
import com.zergatul.scripting.compiler.CapturedLocalVariable;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;
import java.util.stream.Stream;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final List<BoundParameterNode> parameters;
    public final List<CapturedLocalVariable> lambdaCaptured;
    public final List<CapturedAsyncStateMachineFieldVariable> asyncCaptured;
    public final BoundStatementNode body;

    public BoundLambdaExpressionNode(
            SType type,
            List<BoundParameterNode> parameters,
            List<CapturedLocalVariable> lambdaCaptured,
            List<CapturedAsyncStateMachineFieldVariable> asyncCaptured,
            BoundStatementNode body,
            TextRange range
    ) {
        super(NodeType.LAMBDA_EXPRESSION, type, range);
        this.parameters = parameters;
        this.lambdaCaptured = lambdaCaptured;
        this.asyncCaptured = asyncCaptured;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundParameterNode parameter : parameters) {
            parameter.accept(visitor);
        }
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return Stream.concat(List.copyOf(parameters).stream(), Stream.of(body)).toList();
    }
}