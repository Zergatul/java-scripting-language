package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.CollectionExpressionNode;
import com.zergatul.scripting.type.SEmptyCollection;

import java.util.List;

public class BoundEmptyCollectionExpressionNode extends BoundExpressionNode {

    public final Token openBracket;
    public final Token closeBracket;

    public BoundEmptyCollectionExpressionNode(CollectionExpressionNode node) {
        this(node.openBracket, node.closeBracket, node.getRange());
    }

    public BoundEmptyCollectionExpressionNode(Token openBracket, Token closeBracket, TextRange range) {
        super(BoundNodeType.EMPTY_COLLECTION_EXPRESSION, SEmptyCollection.instance, range);
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}