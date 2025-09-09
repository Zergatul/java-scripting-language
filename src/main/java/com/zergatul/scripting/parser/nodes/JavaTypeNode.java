package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class JavaTypeNode extends TypeNode {

    public final Token java;
    public final Token openBracket;
    public final JavaQualifiedTypeNameNode name;
    public final Token closeBracket;

    public JavaTypeNode(Token java, Token openBracket, JavaQualifiedTypeNameNode name, Token closeBracket) {
        super(ParserNodeType.JAVA_TYPE, TextRange.combine(java, closeBracket));
        this.java = java;
        this.openBracket = openBracket;
        this.name = name;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        name.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(java, openBracket, name, closeBracket);
    }
}