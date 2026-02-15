package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundGeneratorFinallyDispatchNode extends BoundStatementNode {

    public BoundGeneratorFinallyDispatchNode() {
        super(BoundNodeType.GENERATOR_FINALLY_DISPATCH, TextRange.MISSING);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}