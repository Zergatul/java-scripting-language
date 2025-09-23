package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.AssignmentStatementNode;

import java.util.List;

public class BoundAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode operator;
    public final BoundExpressionNode right;
    public final Token semicolon;

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right) {
        this(left, operator, right, null, null);
    }

    public BoundAssignmentStatementNode(AssignmentStatementNode node, BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right) {
        this(left, operator, right, node.semicolon, node.getRange());
    }

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right, Token semicolon, TextRange range) {
        super(BoundNodeType.ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.operator = operator;
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
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public boolean isOpen() {
        return semicolon == null || semicolon.isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}