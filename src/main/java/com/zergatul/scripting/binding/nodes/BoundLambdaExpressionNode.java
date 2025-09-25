package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.LambdaExpressionNode;
import com.zergatul.scripting.symbols.CapturedVariable;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.SType;

import java.util.List;
import java.util.stream.Stream;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final LambdaExpressionNode syntaxNode;
    public final List<BoundParameterNode> parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;
    public final List<CapturedVariable> captured;

    public BoundLambdaExpressionNode(
            List<BoundParameterNode> parameters,
            BoundStatementNode body,
            SType type
    ) {
        this(null, parameters, body, type, List.of(), List.of(), null);
    }

    public BoundLambdaExpressionNode(
            LambdaExpressionNode node,
            List<BoundParameterNode> parameters,
            BoundStatementNode body,
            SType type,
            List<LiftedVariable> lifted,
            List<CapturedVariable> captured
    ) {
        this(node, parameters, body, type, lifted, captured, node.getRange());
    }

    public BoundLambdaExpressionNode(
            LambdaExpressionNode node,
            List<BoundParameterNode> parameters,
            BoundStatementNode body,
            SType type,
            List<LiftedVariable> lifted,
            List<CapturedVariable> captured,
            TextRange range
    ) {
        super(BoundNodeType.LAMBDA_EXPRESSION, type, range);
        this.syntaxNode = node;
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
    public boolean isOpen() {
        return body.isOpen();
    }

    @Override
    public List<BoundNode> getChildren() {
        return Stream.concat(parameters.stream(), Stream.of(body)).toList();
    }
}