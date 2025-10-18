package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.BlockStatementNode;

import java.util.List;

public class BoundBlockStatementNode extends BoundStatementNode {

    public final BlockStatementNode syntaxNode;
    public final List<BoundStatementNode> statements;

    public BoundBlockStatementNode(BoundStatementNode statement1, BoundStatementNode statement2) {
        this(SyntaxFactory.missingBlockStatement(), List.of(statement1, statement2), TextRange.MISSING);
    }

    public BoundBlockStatementNode(List<BoundStatementNode> statements) {
        this(SyntaxFactory.missingBlockStatement(), statements, TextRange.MISSING);
    }

    public BoundBlockStatementNode(BlockStatementNode node, List<BoundStatementNode> statements) {
        this(node, statements, node.getRange());
    }

    public BoundBlockStatementNode(BlockStatementNode node, List<BoundStatementNode> statements, TextRange range) {
        super(BoundNodeType.BLOCK_STATEMENT, range);
        this.syntaxNode = node;
        this.statements = statements;
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