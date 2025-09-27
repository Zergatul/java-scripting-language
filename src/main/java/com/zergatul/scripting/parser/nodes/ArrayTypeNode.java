package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ArrayTypeNode extends TypeNode {

    public final TypeNode underlying;
    public final Token openBracket;
    public final Token closeBracket;

    public ArrayTypeNode(TypeNode underlying, Token openBracket, Token closeBracket, TextRange range) {
        super(ParserNodeType.ARRAY_TYPE, range);
        this.underlying = underlying;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        underlying.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(underlying, openBracket, closeBracket);
    }
}