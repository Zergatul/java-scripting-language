package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SGenericFunction;

import java.util.List;

public class BoundFunctionTypeNode extends BoundTypeNode {

    public final Token leftParenthesis;
    public final Token rightParenthesis;
    public final BoundTypeNode returnTypeNode;

    public BoundFunctionTypeNode(Token leftParenthesis, Token rightParenthesis, BoundTypeNode returnTypeNode, SGenericFunction functionType, TextRange range) {
        super(NodeType.FUNCTION_TYPE, functionType, range);
        this.leftParenthesis = leftParenthesis;
        this.rightParenthesis = rightParenthesis;
        this.returnTypeNode = returnTypeNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(returnTypeNode);
    }
}