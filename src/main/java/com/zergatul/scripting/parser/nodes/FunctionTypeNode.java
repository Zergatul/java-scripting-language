package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class FunctionTypeNode extends TypeNode {

    public final Token leftParenthesis;
    public final List<TypeNode> parameterTypes;
    public final Token rightParenthesis;
    public final TypeNode returnTypeNode;

    public FunctionTypeNode(Token leftParenthesis, List<TypeNode> parameterTypes, Token rightParenthesis, TypeNode returnTypeNode, TextRange range) {
        super(NodeType.FUNCTION_TYPE, range);
        this.leftParenthesis = leftParenthesis;
        this.parameterTypes = parameterTypes;
        this.rightParenthesis = rightParenthesis;
        this.returnTypeNode = returnTypeNode;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }
}
