package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ArgumentsListNode extends ParserNode {

    public final Token openParen;
    public final SeparatedList<ExpressionNode> arguments;
    public final Token closeParen;

    public ArgumentsListNode(Token openParen, SeparatedList<ExpressionNode> arguments, Token closeParen) {
        super(ParserNodeType.ARGUMENTS_LIST, TextRange.combine(openParen, closeParen));
        this.openParen = openParen;
        this.arguments = arguments;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode expression : arguments.getNodes()) {
            expression.accept(visitor);
        }
    }
}