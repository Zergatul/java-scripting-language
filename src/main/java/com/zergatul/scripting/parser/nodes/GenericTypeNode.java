package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class GenericTypeNode extends TypeNode {

    public final TypeNode rawType;
    public final Token openBracket;
    public final SeparatedList<TypeNode> arguments;
    public final Token closeBracket;

    public GenericTypeNode(TypeNode rawType, Token openBracket, SeparatedList<TypeNode> arguments, Token closeBracket, TextRange range) {
        super(ParserNodeType.GENERIC_TYPE, range);
        this.rawType = rawType;
        this.openBracket = openBracket;
        this.arguments = arguments;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        rawType.accept(visitor);
        arguments.getNodes().forEach(node -> node.accept(visitor));
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> children = new ArrayList<>();
        children.add(rawType);
        children.add(openBracket);
        children.addAll(arguments.getNodes());
        children.add(closeBracket);
        return children;
    }
}