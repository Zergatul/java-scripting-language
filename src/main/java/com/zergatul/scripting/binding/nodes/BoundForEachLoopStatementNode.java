package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundForEachLoopStatementNode extends BoundStatementNode {

    public final Token lParen;
    public final Token rParen;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode iterable;
    public final BoundStatementNode body;
    public final LocalVariable index;
    public final LocalVariable length;

    public BoundForEachLoopStatementNode(
            Token lParen,
            Token rParen,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            LocalVariable index,
            LocalVariable length
    ) {
        this(lParen, rParen, typeNode, name, iterable, body, index, length, null);
    }

    public BoundForEachLoopStatementNode(
            Token lParen,
            Token rParen,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            LocalVariable index,
            LocalVariable length,
            TextRange range
    ) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
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
}