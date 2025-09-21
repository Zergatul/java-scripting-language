package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class ArgumentsListNode extends ParserNode {

    public final Token openParen;
    public final List<ExpressionNode> arguments;
    public final Token closeParen;

    public ArgumentsListNode(Token openParen, List<ExpressionNode> arguments, Token closeParen, TextRange range) {
        super(ParserNodeType.ARGUMENTS_LIST, range);
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
        for (ExpressionNode expression : arguments) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArgumentsListNode other) {
            return Objects.equals(other.arguments, arguments) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}