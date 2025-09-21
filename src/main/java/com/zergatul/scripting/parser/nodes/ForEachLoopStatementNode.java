package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ForEachLoopStatementNode extends StatementNode {

    public final Token openParen;
    public final TypeNode typeNode;
    public final NameExpressionNode name;
    public final ExpressionNode iterable;
    public final Token closeParen;
    public final StatementNode body;

    public ForEachLoopStatementNode(Token openParen, TypeNode typeNode, NameExpressionNode name, ExpressionNode iterable, Token closeParen, StatementNode body, TextRange range) {
        super(ParserNodeType.FOREACH_LOOP_STATEMENT, range);
        this.openParen = openParen;
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.closeParen = closeParen;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForEachLoopStatementNode other) {
            return  other.openParen.equals(openParen) &&
                    other.typeNode.equals(typeNode) &&
                    other.name.equals(name) &&
                    other.iterable.equals(iterable) &&
                    other.closeParen.equals(closeParen) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}