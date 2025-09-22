package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ArrayInitializerExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final TypeNode typeNode;
    public final Token openBrace;
    public final List<ExpressionNode> items;
    public final Token closeBrace;

    public ArrayInitializerExpressionNode(
            Token keyword,
            TypeNode typeNode,
            Token openBrace,
            List<ExpressionNode> items,
            Token closeBrace,
            TextRange range
    ) {
        super(ParserNodeType.ARRAY_INITIALIZER_EXPRESSION, range);
        this.keyword = keyword;
        this.typeNode = typeNode;
        this.openBrace = openBrace;
        this.items = items;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode expression : items) {
            expression.accept(visitor);
        }
    }
}