package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.DeclarationPatternNode;

import java.util.List;

public class BoundDeclarationPatternNode extends BoundPatternNode {

    public final DeclarationPatternNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final BoundSymbolNode symbolNode;

    public BoundDeclarationPatternNode(DeclarationPatternNode node, BoundTypeNode typeNode, BoundSymbolNode symbolNode, TextRange range) {
        super(BoundNodeType.DECLARATION_PATTERN, range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.symbolNode = symbolNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        symbolNode.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, symbolNode);
    }
}
