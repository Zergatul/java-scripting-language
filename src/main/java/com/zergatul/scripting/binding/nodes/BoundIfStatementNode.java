package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.IfStatementNode;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final Token ifToken;
    public final Token openParen;
    public final Token closeParen;
    public final BoundExpressionNode condition;
    public final BoundStatementNode thenStatement;
    public final Token elseToken;
    public final BoundStatementNode elseStatement;

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement) {
        this(null, null, null, condition, thenStatement, null, null, null);
    }

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement, IfStatementNode node) {
        this(node.ifToken, node.openParen, node.closeParen, condition, thenStatement, node.elseToken, elseStatement, node.getRange());
    }

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(null, null, null, condition, thenStatement, null, elseStatement, null);
    }

    public BoundIfStatementNode(
            Token ifToken,
            Token openParen,
            Token closeParen,
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            Token elseToken,
            BoundStatementNode elseStatement,
            TextRange range
    ) {
        super(NodeType.IF_STATEMENT, range);
        this.ifToken = ifToken;
        this.openParen = openParen;
        this.closeParen = closeParen;
        this.condition = condition;
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