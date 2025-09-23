package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.nodes.AssignmentOperatorNode;

import java.util.List;

public class BoundAssignmentOperatorNode extends BoundNode {

    public final Token token;
    public final AssignmentOperator operator;

    public BoundAssignmentOperatorNode(AssignmentOperator operator) {
        this(null, operator, null);
    }

    public BoundAssignmentOperatorNode(AssignmentOperatorNode node) {
        this(node.token, node.operator, node.getRange());
    }

    public BoundAssignmentOperatorNode(Token token, AssignmentOperator operator, TextRange range) {
        super(BoundNodeType.ASSIGNMENT_OPERATOR, range);
        this.token = token;
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