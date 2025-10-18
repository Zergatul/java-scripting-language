package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AssignmentStatementNode extends StatementNode {

    public final ExpressionNode left;
    public final AssignmentOperatorNode operator;
    public final ExpressionNode right;
    @Nullable public final Token semicolon;

    public AssignmentStatementNode(
            ExpressionNode left,
            AssignmentOperatorNode operator,
            ExpressionNode right,
            @Nullable Token semicolon,
            TextRange range
    ) {
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
        if (semicolon != null) {
            return List.of(left, operator, right, semicolon);
        } else {
            return List.of(left, operator, right);
        }
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new AssignmentStatementNode(left, operator, right, semicolon, TextRange.combine(getRange(), semicolon.getRange()));
    }
}