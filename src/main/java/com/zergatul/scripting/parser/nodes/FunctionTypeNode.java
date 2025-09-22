package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class FunctionTypeNode extends TypeNode {

    public final ValueToken fn;
    public final Token openBracket;
    public final Token openParen;
    public final SeparatedList<TypeNode> parameterTypes;
    public final Token closeParen;
    public final Token arrow;
    public final TypeNode returnTypeNode;
    public final Token closeBracket;

    public FunctionTypeNode(
            ValueToken fn,
            Token openBracket,
            Token openParen,
            SeparatedList<TypeNode> parameterTypes,
            Token closeParen,
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
}