package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundAssignmentOperatorNode extends BoundNode {

    public final AssignmentOperator operator;

    public BoundAssignmentOperatorNode(AssignmentOperator operator) {
        this(operator, null);
    }

    public BoundAssignmentOperatorNode(AssignmentOperator operator, TextRange range) {
        super(NodeType.ASSIGNMENT_OPERATOR, range);
        this.operator = operator;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}