package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ArrayCreationExpressionNode;
import com.zergatul.scripting.type.SArrayType;

import java.util.List;

public class BoundArrayCreationExpressionNode extends BoundExpressionNode {

    public final Token keyword;
    public final BoundTypeNode typeNode;
    public final Token openBracket;
    public final BoundExpressionNode lengthExpression;
    public final Token closeBracket;

    public BoundArrayCreationExpressionNode(ArrayCreationExpressionNode node, BoundTypeNode typeNode, BoundExpressionNode lengthExpression) {
        this(node.keyword, typeNode, node.openBracket, lengthExpression, node.closeBracket, node.getRange());
    }

    public BoundArrayCreationExpressionNode(
            Token keyword,
            BoundTypeNode typeNode,
            Token openBracket,
            BoundExpressionNode lengthExpression,
            Token closeBracket,
            TextRange range
    ) {
        super(BoundNodeType.ARRAY_CREATION_EXPRESSION, new SArrayType(typeNode.type), range);
        this.keyword = keyword;
        this.typeNode = typeNode;
        this.openBracket = openBracket;
        this.lengthExpression = lengthExpression;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        lengthExpression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, lengthExpression);
    }
}