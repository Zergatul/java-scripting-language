package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ForEachLoopStatementNode;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public class BoundForEachLoopStatementNode extends BoundStatementNode {

    public final ForEachLoopStatementNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode iterable;
    public final BoundStatementNode body;
    public final SymbolRef index;
    public final SymbolRef length;

    public BoundForEachLoopStatementNode(
            ForEachLoopStatementNode node,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            SymbolRef index,
            SymbolRef length
    ) {
        this(node,  typeNode, name, iterable, body, index, length, node.getRange());
    }

    public BoundForEachLoopStatementNode(
            ForEachLoopStatementNode node,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            SymbolRef index,
            SymbolRef length,
            TextRange range
    ) {
        super(BoundNodeType.FOREACH_LOOP_STATEMENT, range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.body = body;
        this.index = index;
        this.length = length;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        name.accept(visitor);
        iterable.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, name, iterable, body);
    }

    public BoundForEachLoopStatementNode withBody(BoundStatementNode body) {
        return new BoundForEachLoopStatementNode(syntaxNode, typeNode, name, iterable, body, index, length, getRange());
    }
}