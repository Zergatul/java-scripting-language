package com.zergatul.scripting.parser;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.parser.nodes.PatternNode;
import com.zergatul.scripting.parser.nodes.TypeNode;

import java.util.List;

public class DeclarationPatternNode extends PatternNode {

    public final TypeNode typeNode;
    public final ValueToken identifier;

    public DeclarationPatternNode(TypeNode typeNode, ValueToken identifier) {
        super(ParserNodeType.DECLARATION_PATTERN, TextRange.combine(typeNode, identifier));
        this.typeNode = typeNode;
        this.identifier = identifier;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of();
    }
}