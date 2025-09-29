package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ArrayCreationExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final TypeNode typeNode;
    public final Token openBracket;
    public final ExpressionNode lengthExpression;
    public final Token closeBracket;

    public ArrayCreationExpressionNode(
            Token keyword,
            TypeNode typeNode,
            Token openBracket,
            ExpressionNode lengthExpression,
            Token closeBracket,
            TextRange range
    ) {
        super(ParserNodeType.ARRAY_CREATION_EXPRESSION, range);
        this.keyword = keyword;
        this.typeNode = typeNode;
        this.openBracket = openBracket;
        this.lengthExpression = lengthExpression;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        lengthExpression.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, typeNode, openBracket, lengthExpression, closeBracket);
    }
}