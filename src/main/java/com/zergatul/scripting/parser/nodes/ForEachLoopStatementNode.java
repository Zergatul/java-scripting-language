package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ForEachLoopStatementNode extends StatementNode {

    public final Token lParen;
    public final Token rParen;
    public final TypeNode typeNode;
    public final NameExpressionNode name;
    public final ExpressionNode iterable;
    public final StatementNode body;

    public ForEachLoopStatementNode(Token lParen, Token rParen, TypeNode typeNode, NameExpressionNode name, ExpressionNode iterable, StatementNode body, TextRange range) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        name.accept(visitor);
        iterable.accept(visitor);
        body.accept(visitor);
    }
}