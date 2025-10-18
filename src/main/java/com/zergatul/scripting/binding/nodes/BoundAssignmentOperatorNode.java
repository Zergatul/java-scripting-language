package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.AssignmentOperatorNode;

import java.util.List;

public class BoundAssignmentOperatorNode extends BoundNode {

    public final AssignmentOperatorNode syntaxNode;
    public final AssignmentOperator operator;

    public BoundAssignmentOperatorNode(AssignmentOperator operator) {
        this(SyntaxFactory.missingAssignmentOperator(), operator, TextRange.MISSING);
    }

    public BoundAssignmentOperatorNode(AssignmentOperatorNode node) {
        this(node, node.operator, node.getRange());
    }

    public BoundAssignmentOperatorNode(AssignmentOperatorNode node, AssignmentOperator operator, TextRange range) {
        super(BoundNodeType.ASSIGNMENT_OPERATOR, range);
        this.syntaxNode = node;
        this.operator = operator;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}