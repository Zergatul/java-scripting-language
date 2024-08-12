package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundFunctionNode extends BoundNode {

    public final boolean isAsync;
    public final BoundTypeNode returnType;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundBlockStatementNode block;

    public BoundFunctionNode(boolean isAsync, BoundTypeNode returnType, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundBlockStatementNode block, TextRange range) {
        super(NodeType.FUNCTION, range);
        this.isAsync = isAsync;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.block = block;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        returnType.accept(visitor);
        name.accept(visitor);
        parameters.accept(visitor);
        block.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(returnType, name, parameters, block);
    }
}