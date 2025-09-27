package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class AssignmentStatementNode extends StatementNode {

    public final ExpressionNode left;
    public final AssignmentOperatorNode operator;
    public final ExpressionNode right;
    public final Token semicolon;

    public AssignmentStatementNode(ExpressionNode left, AssignmentOperatorNode operator, ExpressionNode right, Token semicolon, TextRange range) {
        super(ParserNodeType.ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.semicolon = semicolon;
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
    public List<Locatable> getChildNodes() {
        return List.of(left, operator, right, semicolon);
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new AssignmentStatementNode(left, operator, right, semicolon, TextRange.combine(getRange(), semicolon.getRange()));
    }
}