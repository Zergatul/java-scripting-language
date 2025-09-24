package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ForEachLoopStatementNode;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public class BoundForEachLoopStatementNode extends BoundStatementNode {

    public final Token keyword;
    public final Token openParen;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final Token in;
    public final BoundExpressionNode iterable;
    public final Token closeParen;
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
        this(node.keyword, node.openParen, typeNode, name, node.in, iterable, node.closeParen, body, index, length, node.getRange());
    }

    public BoundForEachLoopStatementNode(
            Token keyword,
            Token openParen,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            Token in,
            BoundExpressionNode iterable,
            Token closeParen,
            BoundStatementNode body,
            SymbolRef index,
            SymbolRef length,
            TextRange range
    ) {
        super(BoundNodeType.FOREACH_LOOP_STATEMENT, range);
        this.keyword = keyword;
        this.openParen = openParen;
        this.typeNode = typeNode;
        this.name = name;
        this.in = in;
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

    public BoundForEachLoopStatementNode withBody(BoundStatementNode body) {
        return new BoundForEachLoopStatementNode(keyword, openParen, typeNode, name, in, iterable, closeParen, body, index, length, getRange());
    }
}