package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundGeneratorFinallyExitNode extends BoundStatementNode {

    public final StateBoundary catchState;

    public BoundGeneratorFinallyExitNode(StateBoundary catchState) {
        super(BoundNodeType.GENERATOR_FINALLY_EXIT, TextRange.MISSING);
        this.catchState = catchState;
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