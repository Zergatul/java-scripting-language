package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.StatementsListNode;
import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.List;

public class BoundStatementsListNode extends BoundStatementNode {

    public final StatementsListNode syntaxNode;
    public final List<BoundVariableDeclarationNode> prepend;
    public final List<BoundStatementNode> statements;
    public final List<LiftedVariable> lifted;

    public BoundStatementsListNode(List<BoundStatementNode> statements) {
        this(SyntaxFactory.missingStatementsList(), List.of(), statements, List.of(), TextRange.MISSING);
    }

    public BoundStatementsListNode(StatementsListNode node, List<BoundStatementNode> statements, List<LiftedVariable> lifted) {
        this(node, List.of(), statements, lifted, node.getRange());
    }

    public BoundStatementsListNode(StatementsListNode node, List<BoundVariableDeclarationNode> prepend, List<BoundStatementNode> statements, List<LiftedVariable> lifted, TextRange range) {
        super(BoundNodeType.STATEMENTS_LIST, range);
        this.syntaxNode = node;
        this.prepend = prepend;
        this.statements = statements;
        this.lifted = lifted;
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
    public boolean isOpen() {
        return !statements.isEmpty() && statements.getLast().isOpen();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(statements);
    }

    public BoundStatementsListNode withPrepend(List<BoundVariableDeclarationNode> prepend) {
        return new BoundStatementsListNode(syntaxNode, prepend, statements, lifted, getRange());
    }
}