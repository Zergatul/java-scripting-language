package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SArrayType;

import java.util.List;

public class BoundArrayTypeNode extends BoundTypeNode {

    public final BoundTypeNode underlying;

    public BoundArrayTypeNode(BoundTypeNode underlying, TextRange range) {
        super(NodeType.ARRAY_TYPE, new SArrayType(underlying.type), range);
        this.underlying = underlying;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        underlying.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(underlying);
    }
}