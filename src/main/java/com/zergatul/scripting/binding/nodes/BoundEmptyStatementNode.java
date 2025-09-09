package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.EmptyStatementNode;

import java.util.List;

public class BoundEmptyStatementNode extends BoundStatementNode {

    public final EmptyStatementNode syntaxNode;

    public BoundEmptyStatementNode(EmptyStatementNode node) {
        this(node, node.getRange());
    }

    public BoundEmptyStatementNode(EmptyStatementNode node, TextRange range) {
        super(BoundNodeType.EMPTY_STATEMENT, range);
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