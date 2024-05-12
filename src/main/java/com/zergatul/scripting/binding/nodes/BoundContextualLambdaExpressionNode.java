package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.LambdaExpressionNode;
import com.zergatul.scripting.type.SContextualLambda;

import java.util.List;

public class BoundContextualLambdaExpressionNode extends BoundExpressionNode {

    public LambdaExpressionNode expression;

    public BoundContextualLambdaExpressionNode(LambdaExpressionNode expression) {
        super(NodeType.CONTEXTUAL_LAMBDA_EXPRESSION, new SContextualLambda(expression.parameters.size()), expression.getRange());
        this.expression = expression;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}