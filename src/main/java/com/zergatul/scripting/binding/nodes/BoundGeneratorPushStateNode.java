package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.GeneratorStackEntryType;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundGeneratorPushStateNode extends BoundStatementNode {

    public final GeneratorStackEntryType type;
    public final StateBoundary state;

    public BoundGeneratorPushStateNode(GeneratorStackEntryType type, StateBoundary state) {
        super(BoundNodeType.GENERATOR_PUSH_STATE, TextRange.MISSING);
        this.type = type;
        this.state = state;
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