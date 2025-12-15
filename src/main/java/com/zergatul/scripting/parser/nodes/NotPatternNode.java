package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class NotPatternNode extends PatternNode {

    public final ValueToken keyword;
    public final PatternNode inner;

    public NotPatternNode(ValueToken keyword, PatternNode inner) {
        super(ParserNodeType.NOT_PATTERN, TextRange.combine(keyword, inner));
        this.keyword = keyword;
        this.inner = inner;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, inner);
    }
}