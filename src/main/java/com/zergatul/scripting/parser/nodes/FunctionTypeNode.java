package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class FunctionTypeNode extends TypeNode {

    public final ValueToken fn;
    public final Token open;
    public final List<TypeNode> parameterTypes;
    public final TypeNode returnTypeNode;
    public final Token close;

    public FunctionTypeNode(ValueToken fn, Token open, List<TypeNode> parameterTypes, TypeNode returnTypeNode, Token close, TextRange range) {
        super(ParserNodeType.FUNCTION_TYPE, range);
        this.fn = fn;
        this.open = open;
        this.parameterTypes = parameterTypes;
        this.returnTypeNode = returnTypeNode;
        this.close = close;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }
}