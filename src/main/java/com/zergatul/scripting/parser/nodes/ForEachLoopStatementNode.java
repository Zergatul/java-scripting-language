package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ForEachLoopStatementNode extends StatementNode {

    public final Token keyword;
    public final Token openParen;
    public final TypeNode typeNode;
    public final NameExpressionNode name;
    public final Token in;
    public final ExpressionNode iterable;
    public final Token closeParen;
    public final StatementNode body;

    public ForEachLoopStatementNode(
            Token keyword,
            Token openParen,
            TypeNode typeNode,
            NameExpressionNode name,
            Token in,
            ExpressionNode iterable,
            Token closeParen,
            StatementNode body
    ) {
        super(ParserNodeType.FOREACH_LOOP_STATEMENT, TextRange.combine(keyword, body));
        this.keyword = keyword;
        this.openParen = openParen;
        this.typeNode = typeNode;
        this.name = name;
        this.in = in;
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
    public List<Locatable> getChildNodes() {
        return List.of(keyword, openParen, typeNode, name, in, iterable, closeParen, body);
    }
}