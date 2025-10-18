package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.InvalidExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.type.SUnknown;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundInvalidExpressionNode extends BoundExpressionNode {

    @Nullable public final InvalidExpressionNode syntaxNode;
    public final List<BoundExpressionNode> children;
    public final List<ParserNode> unboundNodes;
    // LookupResultKind resultKind;
    // List<Symbol> candidateSymbols;

    public BoundInvalidExpressionNode(List<BoundExpressionNode> children, TextRange range) {
        this(null, children, List.of(), range);
    }

    public BoundInvalidExpressionNode(List<BoundExpressionNode> children, List<ParserNode> unboundNodes, TextRange range) {
        this(null, children, unboundNodes, range);
    }

    public BoundInvalidExpressionNode(
            @Nullable InvalidExpressionNode node,
            List<BoundExpressionNode> children,
            List<ParserNode> unboundNodes,
            TextRange range
    ) {
        super(BoundNodeType.INVALID_EXPRESSION, SUnknown.instance, range);
        this.syntaxNode = node;
        this.children = children;
        this.unboundNodes = unboundNodes;
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