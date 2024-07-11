package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CapturedLocalVariable;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;
import java.util.stream.Stream;

public class BoundLambdaExpressionNode extends BoundExpressionNode {

    public final List<BoundParameterNode> parameters;
    public final List<CapturedLocalVariable> captured;
    public final BoundStatementNode body;

    public BoundLambdaExpressionNode(SType type, List<BoundParameterNode> parameters, List<CapturedLocalVariable> captured, BoundStatementNode body, TextRange range) {
        super(NodeType.LAMBDA_EXPRESSION, type, range);
        this.parameters = parameters;
        this.captured = captured;
        this.body = body;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public List<BoundNode> getChildren() {
        return Stream.concat(List.copyOf(parameters).stream(), Stream.of(body)).toList();
    }
}