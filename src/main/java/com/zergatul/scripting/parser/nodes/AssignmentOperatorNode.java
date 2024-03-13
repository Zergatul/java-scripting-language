package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.NodeType;

public class AssignmentOperatorNode extends Node {

    public final AssignmentOperator operator;

    public AssignmentOperatorNode(AssignmentOperator operator, TextRange range) {
        super(NodeType.ASSIGNMENT_OPERATOR, range);
        this.operator = operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssignmentOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}