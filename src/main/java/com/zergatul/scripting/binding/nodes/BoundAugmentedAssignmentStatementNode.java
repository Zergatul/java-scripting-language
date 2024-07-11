package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundAugmentedAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode assignmentOperator;
    public final BoundBinaryOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundAugmentedAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BoundBinaryOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(NodeType.AUGMENTED_ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.assignmentOperator = assignmentOperator;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean isAsync() {
        return left.isAsync() || right.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, assignmentOperator, operator, right);
    }
}