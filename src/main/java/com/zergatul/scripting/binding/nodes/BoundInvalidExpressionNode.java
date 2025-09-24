package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidExpressionNode extends BoundExpressionNode {

    public final List<BoundExpressionNode> children;
    // LookupResultKind resultKind;
    // List<Symbol> candidateSymbols;

    public BoundInvalidExpressionNode(List<BoundExpressionNode> children, TextRange range) {
        super(BoundNodeType.INVALID_EXPRESSION, SUnknown.instance, range);
        this.children = children;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(children.toArray(BoundNode[]::new));
    }
}