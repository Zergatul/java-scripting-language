package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundGeneratorReturnNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundGeneratorReturnNode(BoundExpressionNode expression) {
        super(NodeType.GENERATOR_RETURN, null);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return this;
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}