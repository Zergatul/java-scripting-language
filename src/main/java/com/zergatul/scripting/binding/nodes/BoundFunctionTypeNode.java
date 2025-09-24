package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.FunctionTypeNode;
import com.zergatul.scripting.type.SGenericFunction;

import java.util.List;

public class BoundFunctionTypeNode extends BoundTypeNode {

    public final ValueToken fn;
    public final Token openBracket;
    public final Token openParen;
    public final BoundSeparatedList<BoundTypeNode> parameterTypeNodes;
    public final Token closeParen;
    public final Token arrow;
    public final BoundTypeNode returnTypeNode;
    public final Token closeBracket;

    public BoundFunctionTypeNode(
            FunctionTypeNode node,
            BoundSeparatedList<BoundTypeNode> parameterTypeNodes,
            BoundTypeNode returnTypeNode,
            SGenericFunction functionType
    ) {
        this(node.fn, node.openBracket, node.openParen, parameterTypeNodes, node.closeParen, node.arrow, returnTypeNode, node.closeBracket, functionType, node.getRange());
    }

    public BoundFunctionTypeNode(
            ValueToken fn,
            Token openBracket,
            Token openParen,
            BoundSeparatedList<BoundTypeNode> parameterTypeNodes,
            Token closeParen,
            Token arrow,
            BoundTypeNode returnTypeNode,
            Token closeBracket,
            SGenericFunction functionType,
            TextRange range
    ) {
        super(BoundNodeType.FUNCTION_TYPE, functionType, range);
        this.fn = fn;
        this.openBracket = openBracket;
        this.openParen = openParen;
        this.parameterTypeNodes = parameterTypeNodes;
        this.closeParen = closeParen;
        this.arrow = arrow;
        this.returnTypeNode = returnTypeNode;
        this.closeBracket = closeBracket;
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