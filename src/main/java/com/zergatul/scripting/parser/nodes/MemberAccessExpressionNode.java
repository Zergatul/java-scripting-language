package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class MemberAccessExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final Token dot;
    public final NameExpressionNode name;

    public MemberAccessExpressionNode(ExpressionNode callee, Token dot, NameExpressionNode name) {
        super(ParserNodeType.MEMBER_ACCESS_EXPRESSION, TextRange.combine(callee, name));
        this.callee = callee;
        this.dot = dot;
        this.name = name;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        callee.accept(visitor);
        name.accept(visitor);
    }
}