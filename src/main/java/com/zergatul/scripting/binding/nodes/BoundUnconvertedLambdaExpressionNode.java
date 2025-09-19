package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.nodes.LambdaExpressionNode;
import com.zergatul.scripting.type.SUnconvertedLambda;

import java.util.List;

public class BoundUnconvertedLambdaExpressionNode extends BoundExpressionNode {

    public final LambdaExpressionNode lambda;

    public BoundUnconvertedLambdaExpressionNode(LambdaExpressionNode lambda, SUnconvertedLambda type, TextRange range) {
        super(NodeType.UNCONVERTED_LAMBDA, type, range);
        this.lambda = lambda;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}