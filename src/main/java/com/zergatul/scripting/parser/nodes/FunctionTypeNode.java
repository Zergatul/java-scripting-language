package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FunctionTypeNode extends TypeNode {

    public final ValueToken fn;
    public final Token openBracket;
    @Nullable
    public final Token openParen;
    public final SeparatedList<TypeNode> parameterTypes;
    @Nullable
    public final Token closeParen;
    public final Token arrow;
    public final TypeNode returnTypeNode;
    public final Token closeBracket;

    public FunctionTypeNode(
            ValueToken fn,
            Token openBracket,
            @Nullable Token openParen,
            SeparatedList<TypeNode> parameterTypes,
            @Nullable Token closeParen,
            Token arrow,
            TypeNode returnTypeNode,
            Token closeBracket
    ) {
        super(ParserNodeType.FUNCTION_TYPE, TextRange.combine(fn, closeBracket));
        this.fn = fn;
        this.openBracket = openBracket;
        this.openParen = openParen;
        this.parameterTypes = parameterTypes;
        this.closeParen = closeParen;
        this.arrow = arrow;
        this.returnTypeNode = returnTypeNode;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {}

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(fn);
        nodes.add(openBracket);
        if (openParen != null) {
            nodes.add(openParen);
        }
        nodes.addAll(parameterTypes.getChildNodes());
        if (closeParen != null) {
            nodes.add(closeParen);
        }
        nodes.add(arrow);
        nodes.add(returnTypeNode);
        nodes.add(closeBracket);
        return nodes;
    }
}