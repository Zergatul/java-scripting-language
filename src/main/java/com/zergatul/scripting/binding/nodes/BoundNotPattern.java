package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NotPatternNode;

import java.util.List;

public class BoundNotPattern extends BoundPatternNode {

    public final NotPatternNode syntaxNode;
    public final BoundPatternNode inner;

    public BoundNotPattern(NotPatternNode node, BoundPatternNode inner, TextRange range) {
        super(BoundNodeType.NOT_PATTERN, range);
        this.syntaxNode = node;
        this.inner = inner;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(inner);
    }
}