package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.CapturedVariable;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.SType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final List<BoundParameterNode> parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;
    public final List<CapturedVariable> captured;

    public BoundLambdaExpressionNode(
            SType type,
            List<BoundParameterNode> parameters,
            BoundStatementNode body
    ) {
        this(type, parameters, body, List.of(), List.of(), null);
    }

    public BoundLambdaExpressionNode(
            SType type,
            List<BoundParameterNode> parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted,
            List<CapturedVariable> captured,
            TextRange range
    ) {
        super(NodeType.LAMBDA_EXPRESSION, type, range);
        this.parameters = parameters;
        this.body = body;
        this.lifted = lifted;
        this.captured = captured;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundLambdaExpressionNode other) {
            return  Objects.equals(other.parameters, parameters) &&
                    other.body.equals(body) &&
                    Objects.equals(other.lifted, lifted) &&
                    Objects.equals(other.captured, captured) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}