package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundSetGeneratorBoundaryNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundSetGeneratorBoundaryNode(BoundExpressionNode expression) {
        super(BoundNodeType.SET_GENERATOR_BOUNDARY, TextRange.MISSING);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}