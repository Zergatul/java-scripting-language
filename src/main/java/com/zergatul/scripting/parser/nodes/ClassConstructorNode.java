package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ClassConstructorNode extends ClassMemberNode {

    public final Token keyword;
    public final ParameterListNode parameters;
    public final Token arrow;
    public final StatementNode body;

    public ClassConstructorNode(Token keyword, ParameterListNode parameters, Token arrow, StatementNode body, TextRange range) {
        super(ParserNodeType.CLASS_CONSTRUCTOR, range);
        this.keyword = keyword;
        this.parameters = parameters;
        this.arrow = arrow;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        parameters.accept(visitor);
        body.accept(visitor);
    }
}