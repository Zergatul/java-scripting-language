package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundStaticReferenceExpression extends BoundExpressionNode {

    public final ParserNode syntaxNode;
    public final BoundTypeNode typeNode;

    public BoundStaticReferenceExpression(ParserNode node, BoundTypeNode typeNode, SType type) {
        this(node, typeNode, type, node.getRange());
    }

    public BoundStaticReferenceExpression(ParserNode node, BoundTypeNode typeNode, SType type, TextRange range) {
        super(BoundNodeType.STATIC_REFERENCE, type, range);

        if (node.isNot(ParserNodeType.STATIC_REFERENCE) && node.isNot(ParserNodeType.NAME_EXPRESSION)) {
            throw new InternalException();
        }

        this.syntaxNode = node;
        this.typeNode = typeNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}