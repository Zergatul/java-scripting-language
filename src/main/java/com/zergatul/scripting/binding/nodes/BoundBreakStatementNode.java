package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.BreakStatementNode;

import java.util.List;

public class BoundBreakStatementNode extends BoundStatementNode {

    public final Token keyword;
    public final Token semicolon;

    public BoundBreakStatementNode(BreakStatementNode node) {
        this(node.keyword, node.semicolon, node.getRange());
    }

    public BoundBreakStatementNode(Token keyword, Token semicolon, TextRange range) {
        super(BoundNodeType.BREAK_STATEMENT, range);
        this.keyword = keyword;
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