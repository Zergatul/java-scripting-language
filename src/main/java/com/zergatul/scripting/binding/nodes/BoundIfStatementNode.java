package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.IfStatementNode;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final Token ifToken;
    public final Token openParen;
    public final BoundExpressionNode condition;
    public final Token closeParen;
    public final BoundStatementNode thenStatement;
    public final Token elseToken;
    public final BoundStatementNode elseStatement;

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement) {
        this(null, null, condition, null, thenStatement, null, null, null);
    }

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(null, null, condition, null, thenStatement, null, elseStatement, null);
    }

    public BoundIfStatementNode(IfStatementNode node, BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(node.ifToken, node.openParen, condition, node.closeParen, thenStatement, node.elseToken, elseStatement, node.getRange());
    }

    public BoundIfStatementNode(
            Token ifToken,
            Token openParen,
            BoundExpressionNode condition,
            Token closeParen,
            BoundStatementNode thenStatement,
            Token elseToken,
            BoundStatementNode elseStatement,
            TextRange range
    ) {
        super(BoundNodeType.IF_STATEMENT, range);
        this.ifToken = ifToken;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.thenStatement = thenStatement;
        this.elseToken = elseToken;
        this.elseStatement = elseStatement;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        thenStatement.accept(visitor);
        if (elseStatement != null) {
            elseStatement.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return elseStatement == null ? List.of(condition, thenStatement) : List.of(condition, thenStatement, elseStatement);
    }
}