package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.InvalidExpressionNode;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidExpressionNode extends BoundExpressionNode {

    public final InvalidExpressionNode syntaxNode;
    public final List<BoundExpressionNode> children;
    // LookupResultKind resultKind;
    // List<Symbol> candidateSymbols;

    public BoundInvalidExpressionNode(List<BoundExpressionNode> children, TextRange range) {
        this(null, children, range);
    }

    public BoundInvalidExpressionNode(InvalidExpressionNode node, List<BoundExpressionNode> children, TextRange range) {
        super(BoundNodeType.INVALID_EXPRESSION, SUnknown.instance, range);
        this.syntaxNode = node;
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