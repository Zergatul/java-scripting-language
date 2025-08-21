package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SGenericFunction;

import java.util.List;

public class BoundFunctionTypeNode extends BoundTypeNode {

    public final Token open;
    public final BoundTypeNode returnTypeNode;
    public final List<BoundTypeNode> parameterTypeNodes;
    public final Token close;

    public BoundFunctionTypeNode(Token open, BoundTypeNode returnTypeNode, List<BoundTypeNode> parameterTypeNodes, Token close, SGenericFunction functionType, TextRange range) {
        super(NodeType.FUNCTION_TYPE, functionType, range);
        this.open = open;
        this.returnTypeNode = returnTypeNode;
        this.parameterTypeNodes = parameterTypeNodes;
        this.close = close;
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