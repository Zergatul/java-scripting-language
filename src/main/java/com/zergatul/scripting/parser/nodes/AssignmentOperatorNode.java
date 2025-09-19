package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class AssignmentOperatorNode extends ParserNode {

    public final AssignmentOperator operator;

    public AssignmentOperatorNode(AssignmentOperator operator, TextRange range) {
        super(NodeType.ASSIGNMENT_OPERATOR, range);
        this.operator = operator;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssignmentOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}