package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.ForEachLoopStatementNode;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public class BoundForEachLoopStatementNode extends BoundStatementNode {

    public final Token openParen;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode iterable;
    public final Token closeParen;
    public final BoundStatementNode body;
    public final SymbolRef index;
    public final SymbolRef length;

    public BoundForEachLoopStatementNode(
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            SymbolRef index,
            SymbolRef length,
            ForEachLoopStatementNode node
    ) {
        this(node.openParen, typeNode, name, iterable, node.closeParen, body, index, length, node.getRange());
    }

    public BoundForEachLoopStatementNode(
            Token openParen,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            Token closeParen,
            BoundStatementNode body,
            SymbolRef index,
            SymbolRef length,
            TextRange range
    ) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.openParen = openParen;
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.closeParen = closeParen;
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
}