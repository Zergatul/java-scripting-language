package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class TypeAliasNode extends CompilationUnitMemberNode {

    public final Token keyword;
    public final ValueToken name;
    public final Token equal;
    public final TypeNode typeNode;
    public final Token semicolon;

    public TypeAliasNode(Token keyword, ValueToken name, Token equal, TypeNode typeNode, Token semicolon) {
        this(keyword, name, equal, typeNode, semicolon, TextRange.combine(keyword, semicolon));
    }

    public TypeAliasNode(Token keyword, ValueToken name, Token equal, TypeNode typeNode, Token semicolon, TextRange range) {
        super(ParserNodeType.TYPE_ALIAS, range);
        this.keyword = keyword;
        this.name = name;
        this.equal = equal;
        this.typeNode = typeNode;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, name, equal, typeNode, semicolon);
    }
}