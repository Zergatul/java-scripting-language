package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ContinueStatementNode;

import java.util.List;

public class BoundContinueStatementNode extends BoundStatementNode {

    public final ContinueStatementNode syntaxNode;

    public BoundContinueStatementNode(ContinueStatementNode node) {
        this(node, node.getRange());
    }

    public BoundContinueStatementNode(ContinueStatementNode node, TextRange range) {
        super(BoundNodeType.CONTINUE_STATEMENT, range);
        this.syntaxNode = node;
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