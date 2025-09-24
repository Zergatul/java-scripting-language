package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.LambdaExpressionNode;
import com.zergatul.scripting.symbols.CapturedVariable;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.SType;

import java.util.List;
import java.util.stream.Stream;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final Token openParen;
    public final BoundSeparatedList<BoundParameterNode> parameters;
    public final Token closeParen;
    public final Token arrow;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;
    public final List<CapturedVariable> captured;

    public BoundLambdaExpressionNode(
            BoundSeparatedList<BoundParameterNode> parameters,
            BoundStatementNode body,
            SType type
    ) {
        this(null, parameters, null, null, body, type, List.of(), List.of(), null);
    }

    public BoundLambdaExpressionNode(
            LambdaExpressionNode node,
            BoundSeparatedList<BoundParameterNode> parameters,
            BoundStatementNode body,
            SType type,
            List<LiftedVariable> lifted,
            List<CapturedVariable> captured
    ) {
        this(node.openParen, parameters, node.closeParen, node.arrow, body, type, lifted, captured, node.getRange());
    }

    public BoundLambdaExpressionNode(
            Token openParen,
            BoundSeparatedList<BoundParameterNode> parameters,
            Token closeParen,
            Token arrow,
            BoundStatementNode body,
            SType type,
            List<LiftedVariable> lifted,
            List<CapturedVariable> captured,
            TextRange range
    ) {
        super(BoundNodeType.LAMBDA_EXPRESSION, type, range);
        this.openParen = openParen;
        this.parameters = parameters;
        this.closeParen = closeParen;
        this.arrow = arrow;
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
        for (BoundParameterNode parameter : parameters.getNodes()) {
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
        return Stream.concat(List.copyOf(parameters.getNodes()).stream(), Stream.of(body)).toList();
    }
}