package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.parser.AssignmentOperator;

public class AssignmentStatementNode extends StatementNode {

    public final ExpressionNode left;
    public AssignmentOperator operator;
    public final ExpressionNode right;

    public AssignmentStatementNode(ExpressionNode left, AssignmentOperator operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssignmentStatementNode other) {
            return other.left.equals(left) && other.operator.equals(operator) && other.right.equals(right);
        } else {
            return false;
        }
    }
}