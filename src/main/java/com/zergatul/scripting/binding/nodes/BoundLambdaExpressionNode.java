package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final List<BoundParameter> parameters;
    public final BoundStatementNode body;

    public BoundLambdaExpressionNode(SType type, List<BoundParameter> parameters, BoundStatementNode body, TextRange range) {
        super(NodeType.LAMBDA_EXPRESSION, type, range);
        this.parameters = parameters;
        this.body = body;
    }
}