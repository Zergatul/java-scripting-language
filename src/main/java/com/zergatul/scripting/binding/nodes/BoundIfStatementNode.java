package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final Token lParen;
    public final Token rParen;
    public final BoundExpressionNode condition;
    public final BoundStatementNode thenStatement;
    public final BoundStatementNode elseStatement;

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement) {
        this(null, null, condition, thenStatement, null, null);
    }

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(null, null, condition, thenStatement, elseStatement, null);
    }

    public BoundIfStatementNode(
            Token lParen,
            Token rParen,
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            BoundStatementNode elseStatement,
            TextRange range
    ) {
        super(NodeType.IF_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
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