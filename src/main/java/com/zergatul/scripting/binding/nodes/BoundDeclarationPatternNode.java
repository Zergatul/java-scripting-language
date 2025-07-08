package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundDeclarationPatternNode extends BoundPatternNode {

    public final BoundVariableDeclarationNode declaration;

    public BoundDeclarationPatternNode(BoundVariableDeclarationNode declaration, TextRange range) {
        super(NodeType.DECLARATION_PATTERN, range);
        this.declaration = declaration;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        declaration.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(declaration);
    }
}