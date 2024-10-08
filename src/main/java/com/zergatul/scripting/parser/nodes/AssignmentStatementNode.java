package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class AssignmentStatementNode extends StatementNode {

    public final ExpressionNode left;
    public final AssignmentOperatorNode operator;
    public final ExpressionNode right;

    public AssignmentStatementNode(ExpressionNode left, AssignmentOperatorNode operator, ExpressionNode right, TextRange range) {
        super(NodeType.ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        left.accept(visitor);
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssignmentStatementNode other) {
            return other.left.equals(left) && other.operator.equals(operator) && other.right.equals(right);
        } else {
            return false;
        }
    }

    @Override
    public StatementNode append(Token token) {
        return new AssignmentStatementNode(left, operator, right, TextRange.combine(getRange(), token.getRange()));
    }
}