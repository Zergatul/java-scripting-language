package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.nodes.AssignmentOperatorNode;
import com.zergatul.scripting.parser.nodes.AssignmentStatementNode;
import com.zergatul.scripting.parser.nodes.ExpressionNode;

public class BoundAssignmentStatementNode extends AssignmentStatementNode {
    public BoundAssignmentStatementNode(ExpressionNode left, AssignmentOperatorNode operator, ExpressionNode right, TextRange range) {
        super(left, operator, right, range);
    }
}