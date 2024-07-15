package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundSetGeneratorStateNode extends BoundStatementNode {

    public final StateBoundary boundary;

    public BoundSetGeneratorStateNode(StateBoundary boundary) {
        super(NodeType.SET_GENERATOR_STATE, new SingleLineTextRange(1, 1, 0, 0));
        this.boundary = boundary;
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
