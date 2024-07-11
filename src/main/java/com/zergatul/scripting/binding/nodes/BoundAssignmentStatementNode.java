package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(NodeType.ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean isAsync() {
        return left.isAsync() || right.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}