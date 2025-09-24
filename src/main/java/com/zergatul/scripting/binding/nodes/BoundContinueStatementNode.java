package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ContinueStatementNode;

import java.util.List;

public class BoundContinueStatementNode extends BoundStatementNode {

    public final Token keyword;
    public final Token semicolon;

    public BoundContinueStatementNode(ContinueStatementNode node) {
        this(node.keyword, node.semicolon, node.getRange());
    }

    public BoundContinueStatementNode(Token keyword, Token semicolon, TextRange range) {
        super(BoundNodeType.CONTINUE_STATEMENT, range);
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