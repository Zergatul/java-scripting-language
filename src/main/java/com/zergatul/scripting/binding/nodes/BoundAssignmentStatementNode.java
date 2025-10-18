package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.AssignmentStatementNode;

import java.util.List;

public class BoundAssignmentStatementNode extends BoundStatementNode {

    public final AssignmentStatementNode syntaxNode;
    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right) {
        this(SyntaxFactory.missingAssignmentStatement(), left, operator, right, TextRange.MISSING);
    }

    public BoundAssignmentStatementNode(AssignmentStatementNode node, BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right) {
        this(node, left, operator, right, node.getRange());
    }

    public BoundAssignmentStatementNode(AssignmentStatementNode node, BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(BoundNodeType.ASSIGNMENT_STATEMENT, range);
        this.syntaxNode = node;
        this.left = left;
        this.operator = operator;
        this.right = right;
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
        return syntaxNode.semicolon == null || syntaxNode.semicolon.isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}