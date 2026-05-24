package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.LambdaExpressionNode;
import com.zergatul.scripting.type.SUnconvertedLambda;

import java.util.List;

public class BoundUnconvertedLambdaExpressionNode extends BoundExpressionNode {

    public final LambdaExpressionNode syntaxNode;
    public final List<BoundParameterNode> parameters;

    public BoundUnconvertedLambdaExpressionNode(
            LambdaExpressionNode node,
            List<BoundParameterNode> parameters,
            SUnconvertedLambda type,
            TextRange range
    ) {
        super(BoundNodeType.UNCONVERTED_LAMBDA, type, range);
        this.syntaxNode = node;
        this.parameters = parameters;
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
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(parameters.toArray(BoundNode[]::new));
    }

    @Override
    public boolean isOpen() {
        return syntaxNode.isOpen();
    }
}