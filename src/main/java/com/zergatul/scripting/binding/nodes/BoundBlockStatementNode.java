package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.BlockStatementNode;

import java.util.List;

public class BoundBlockStatementNode extends BoundStatementNode {

    public final Token openBrace;
    public final List<BoundStatementNode> statements;
    public final Token closeBrace;

    public BoundBlockStatementNode(BoundStatementNode statement1, BoundStatementNode statement2) {
        this(null, List.of(statement1, statement2), null, null);
    }

    public BoundBlockStatementNode(List<BoundStatementNode> statements) {
        this(null, statements, null, null);
    }

    public BoundBlockStatementNode(BlockStatementNode node, List<BoundStatementNode> statements) {
        this(node.openBrace, statements, node.closeBrace, node.getRange());
    }

    public BoundBlockStatementNode(Token openBrace, List<BoundStatementNode> statements, Token closeBrace, TextRange range) {
        super(BoundNodeType.BLOCK_STATEMENT, range);
        this.openBrace = openBrace;
        this.statements = statements;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundStatementNode statement : statements) {
            statement.accept(visitor);
        }
    }
    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(statements);
    }
}