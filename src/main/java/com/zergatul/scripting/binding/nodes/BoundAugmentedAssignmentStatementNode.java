package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.AssignmentStatementNode;
import com.zergatul.scripting.type.operation.BinaryOperation;

import java.util.List;

public class BoundAugmentedAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode assignmentOperator;
    public final BinaryOperation operation;
    public final BoundExpressionNode right;
    public final Token semicolon;

    public BoundAugmentedAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BinaryOperation operation, BoundExpressionNode right) {
        this(left, assignmentOperator, operation, right, null, null);
    }

    public BoundAugmentedAssignmentStatementNode(AssignmentStatementNode node, BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BinaryOperation operation, BoundExpressionNode right) {
        this(left, assignmentOperator, operation, right, node.semicolon, node.getRange());
    }

    public BoundAugmentedAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BinaryOperation operation, BoundExpressionNode right, Token semicolon, TextRange range) {
        super(BoundNodeType.AUGMENTED_ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.assignmentOperator = assignmentOperator;
        this.operation = operation;
        this.right = right;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        left.accept(visitor);
        assignmentOperator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, assignmentOperator, right);
    }
}