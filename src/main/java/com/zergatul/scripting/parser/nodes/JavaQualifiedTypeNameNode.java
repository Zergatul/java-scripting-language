package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class JavaQualifiedTypeNameNode extends ParserNode {

    public final List<Token> tokens;
    public final String value;

    public JavaQualifiedTypeNameNode(List<Token> tokens, String value) {
        super(ParserNodeType.JAVA_TYPE_NAME, TextRange.combine(tokens));
        this.tokens = tokens;
        this.value = value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.copyOf(tokens);
    }
}