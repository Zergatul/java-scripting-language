package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.FunctionTypeNode;
import com.zergatul.scripting.type.SGenericFunction;

import java.util.List;

public class BoundFunctionTypeNode extends BoundTypeNode {

    public final FunctionTypeNode syntaxNode;
    public final List<BoundTypeNode> parameterTypeNodes;
    public final BoundTypeNode returnTypeNode;

    public BoundFunctionTypeNode(
            FunctionTypeNode node,
            List<BoundTypeNode> parameterTypeNodes,
            BoundTypeNode returnTypeNode,
            SGenericFunction functionType
    ) {
        this(node, parameterTypeNodes, returnTypeNode, functionType, node.getRange());
    }

    public BoundFunctionTypeNode(
            FunctionTypeNode node,
            List<BoundTypeNode> parameterTypeNodes,
            BoundTypeNode returnTypeNode,
            SGenericFunction functionType,
            TextRange range
    ) {
        super(BoundNodeType.FUNCTION_TYPE, functionType, range);
        this.syntaxNode = node;
        this.parameterTypeNodes = parameterTypeNodes;
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