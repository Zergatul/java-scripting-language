package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundNewExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode typeNode;
    public final BoundExpressionNode lengthExpression;
    public final List<BoundExpressionNode> items;

    public BoundNewExpressionNode(BoundTypeNode typeNode, BoundExpressionNode lengthExpression, List<BoundExpressionNode> items, TextRange range) {
        super(NodeType.NEW_EXPRESSION, typeNode.type, range);
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
        this.items = items;
    }
}