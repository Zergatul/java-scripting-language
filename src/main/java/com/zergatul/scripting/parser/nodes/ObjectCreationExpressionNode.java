package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ObjectCreationExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final TypeNode typeNode;
    public final ArgumentsListNode arguments;

    public ObjectCreationExpressionNode(Token keyword, TypeNode typeNode, ArgumentsListNode arguments) {
        super(ParserNodeType.OBJECT_CREATION_EXPRESSION, TextRange.combine(keyword, arguments));
        this.keyword = keyword;
        this.typeNode = typeNode;
        this.arguments = arguments;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, typeNode, arguments);
    }
}