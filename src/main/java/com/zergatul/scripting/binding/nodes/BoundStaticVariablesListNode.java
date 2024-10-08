package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundStaticVariablesListNode extends BoundNode {

    public final List<BoundVariableDeclarationNode> variables;

    public BoundStaticVariablesListNode(List<BoundVariableDeclarationNode> variables, TextRange range) {
        super(NodeType.STATIC_VARIABLES_LIST, range);
        this.variables = variables;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundVariableDeclarationNode variable : variables) {
            variable.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(variables);
    }
}