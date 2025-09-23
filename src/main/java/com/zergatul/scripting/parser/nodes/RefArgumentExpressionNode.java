package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class RefArgumentExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final NameExpressionNode name;

    public RefArgumentExpressionNode(Token keyword, NameExpressionNode name) {
        super(ParserNodeType.REF_ARGUMENT_EXPRESSION, TextRange.combine(keyword, name));
        this.keyword = keyword;
        this.name = name;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        name.accept(visitor);
    }
}