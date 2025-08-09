package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ForEachLoopStatementNode extends StatementNode {

    public final Token openParenthesis;
    public final TypeNode typeNode;
    public final NameExpressionNode name;
    public final ExpressionNode iterable;
    public final Token closeParenthesis;
    public final StatementNode body;

    public ForEachLoopStatementNode(Token openParenthesis, TypeNode typeNode, NameExpressionNode name, ExpressionNode iterable, Token closeParenthesis, StatementNode body, TextRange range) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.openParenthesis = openParenthesis;
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.closeParenthesis = closeParenthesis;
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
            return  other.openParenthesis.equals(openParenthesis) &&
                    other.typeNode.equals(typeNode) &&
                    other.name.equals(name) &&
                    other.iterable.equals(iterable) &&
                    other.closeParenthesis.equals(closeParenthesis) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}