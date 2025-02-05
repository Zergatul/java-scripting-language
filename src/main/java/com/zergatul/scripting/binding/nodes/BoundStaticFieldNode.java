package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundStaticFieldNode extends BoundCompilationUnitMemberNode {

    public final BoundVariableDeclarationNode declaration;

    public BoundStaticFieldNode(BoundVariableDeclarationNode declaration, TextRange range) {
        super(NodeType.STATIC_FIELD, range);
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