package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundSetGeneratorBoundaryNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundSetGeneratorBoundaryNode(BoundExpressionNode expression) {
        super(NodeType.SET_GENERATOR_BOUNDARY, new SingleLineTextRange(1, 1, 0, 0));
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