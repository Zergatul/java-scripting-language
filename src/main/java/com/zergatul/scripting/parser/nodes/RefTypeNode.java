package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class RefTypeNode extends TypeNode {

    public final Token keyword;
    public final TypeNode underlying;

    public RefTypeNode(Token keyword, TypeNode underlying) {
        super(ParserNodeType.REF_TYPE, TextRange.combine(keyword, underlying));
        this.keyword = keyword;
        this.underlying = underlying;
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
        return List.of(keyword, underlying);
    }
}