package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.EmptyStatementNode;

import java.util.List;

public class BoundEmptyStatementNode extends BoundStatementNode {

    public final Token semicolon;

    public BoundEmptyStatementNode(EmptyStatementNode node) {
        this(node.semicolon, node.getRange());
    }

    public BoundEmptyStatementNode(Token semicolon, TextRange range) {
        super(BoundNodeType.EMPTY_STATEMENT, range);
        this.semicolon = semicolon;
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