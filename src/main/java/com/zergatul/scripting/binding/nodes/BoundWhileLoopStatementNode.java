package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.nodes.WhileLoopStatementNode;

import java.util.List;

public class BoundWhileLoopStatementNode extends BoundStatementNode {

    public final Token keyword;
    public final Token openParen;
    public final BoundExpressionNode condition;
    public final Token closeParen;
    public final BoundStatementNode body;

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body) {
        this(null, null, condition, null, body, null);
    }

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body, WhileLoopStatementNode node) {
        this(node.keyword, node.openParen, condition, node.closeParen, body, node.getRange());
    }

    public BoundWhileLoopStatementNode(Token keyword, Token openParen, BoundExpressionNode condition, Token closeParen, BoundStatementNode body, TextRange range) {
        super(NodeType.WHILE_LOOP_STATEMENT, range);
        this.keyword = keyword;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, body);
    }
}