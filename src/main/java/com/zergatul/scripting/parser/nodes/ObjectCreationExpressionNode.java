package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ObjectCreationExpressionNode extends ExpressionNode {

    public final TypeNode typeNode;
    public final ArgumentsListNode arguments;

    public ObjectCreationExpressionNode(TypeNode typeNode, ArgumentsListNode arguments, TextRange range) {
        super(ParserNodeType.OBJECT_CREATION_EXPRESSION, range);
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
    public boolean equals(Object obj) {
        if (obj instanceof ObjectCreationExpressionNode other) {
            return  other.typeNode.equals(typeNode) &&
                    other.arguments.equals(arguments) &&
                    other.getRange().equals(getRange());
        }
        return super.equals(obj);
    }
}