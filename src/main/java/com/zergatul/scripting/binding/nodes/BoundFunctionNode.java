package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundFunctionNode extends BoundNode {

    public final BoundTypeNode returnType;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundBlockStatementNode block;

    public BoundFunctionNode(BoundTypeNode returnType, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundBlockStatementNode block, TextRange range) {
        super(NodeType.FUNCTION, range);
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.block = block;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(returnType, name, parameters, block);
    }
}