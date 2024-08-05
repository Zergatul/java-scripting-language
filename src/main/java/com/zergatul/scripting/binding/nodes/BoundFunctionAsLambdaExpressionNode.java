package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundFunctionAsLambdaExpressionNode extends BoundExpressionNode {

    public final BoundNameExpressionNode name;

    public BoundFunctionAsLambdaExpressionNode(SType type, BoundNameExpressionNode name, TextRange range) {
        super(NodeType.FUNCTION_AS_LAMBDA, type, range);
        this.name = name;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(name);
    }
}