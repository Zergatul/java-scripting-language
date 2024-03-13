package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.NodeType;

public class BoundAssignmentOperatorNode extends BoundNode {

    public final AssignmentOperator operator;

    public BoundAssignmentOperatorNode(AssignmentOperator operator, TextRange range) {
        super(NodeType.ASSIGNMENT_OPERATOR, range);
        this.operator = operator;
    }
}