package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.BreakStatementNode;

import java.util.List;

public class BoundBreakStatementNode extends BoundStatementNode {

    public final BreakStatementNode syntaxNode;
    public final boolean isInsideLoop;

    public BoundBreakStatementNode(BreakStatementNode node, boolean isInsideLoop) {
        this(node, isInsideLoop, node.getRange());
    }

    public BoundBreakStatementNode(BreakStatementNode node, boolean isInsideLoop, TextRange range) {
        super(BoundNodeType.BREAK_STATEMENT, range);
        this.syntaxNode = node;
        this.isInsideLoop = isInsideLoop;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}