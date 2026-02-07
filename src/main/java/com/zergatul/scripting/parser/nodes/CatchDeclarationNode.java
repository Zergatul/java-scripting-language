package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class CatchDeclarationNode extends ParserNode {

    public final Token openParen;
    public final ValueToken identifier;
    public final Token closeParen;

    public CatchDeclarationNode(Token openParen, ValueToken identifier, Token closeParen) {
        super(ParserNodeType.CATCH_DECLARATION, TextRange.combine(openParen, closeParen));
        this.openParen = openParen;
        this.identifier = identifier;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(openParen, identifier, closeParen);
    }

    @Override
    public boolean isOpen() {
        return closeParen.isMissing();
    }
}