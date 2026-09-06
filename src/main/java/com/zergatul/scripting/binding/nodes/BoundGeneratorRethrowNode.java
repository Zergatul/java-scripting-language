package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class BoundGeneratorRethrowNode extends BoundStatementNode {

    public BoundGeneratorRethrowNode() {
        super(BoundNodeType.GENERATOR_RETHROW, TextRange.MISSING);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return Lists.of();
    }
}