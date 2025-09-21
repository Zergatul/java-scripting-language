package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;

import java.util.List;

public class BoundForLoopStatementNode extends BoundStatementNode {

    public final Token lParen;
    public final Token rParen;
    public final BoundStatementNode init;
    public final BoundExpressionNode condition;
    public final BoundStatementNode update;
    public final BoundStatementNode body;

    public BoundForLoopStatementNode(Token lParen, Token rParen, BoundStatementNode init, BoundExpressionNode condition, BoundStatementNode update, BoundStatementNode body) {
        this(lParen, rParen, init, condition, update, body, null);
    }

    public BoundForLoopStatementNode(Token lParen, Token rParen, BoundStatementNode init, BoundExpressionNode condition, BoundStatementNode update, BoundStatementNode body, TextRange range) {
        super(BoundNodeType.FOR_LOOP_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        init.accept(visitor);
        if (condition != null) {
            condition.accept(visitor);
        }
        update.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        if (condition != null) {
            return List.of(init, condition, update, body);
        } else {
            return List.of(init, update, body);
        }
    }
}