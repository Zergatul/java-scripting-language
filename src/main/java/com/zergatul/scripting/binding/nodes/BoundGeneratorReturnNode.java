package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundGeneratorReturnNode extends BoundStatementNode {

    public BoundGeneratorReturnNode() {
        super(NodeType.GENERATOR_RETURN, new SingleLineTextRange(1, 1, 0, 0));
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
