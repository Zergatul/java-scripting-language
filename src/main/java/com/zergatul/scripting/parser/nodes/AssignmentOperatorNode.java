package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class AssignmentOperatorNode extends ParserNode {

    public final Token token;
    public final AssignmentOperator operator;

    public AssignmentOperatorNode(Token token, AssignmentOperator operator, TextRange range) {
        super(ParserNodeType.ASSIGNMENT_OPERATOR, range);
        this.token = token;
        this.operator = operator;
    }

    public AssignmentOperatorNode(AssignmentOperator operator, TextRange range) {
        super(ParserNodeType.ASSIGNMENT_OPERATOR, range);
        this.operator = operator;
        throw new InternalException();
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}