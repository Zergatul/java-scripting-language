package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ParameterListNode;

import java.util.List;

public class BoundParameterListNode extends BoundNode {

    public final ParameterListNode syntaxNode;
    public final List<BoundParameterNode> parameters;

    public BoundParameterListNode(ParameterListNode node, List<BoundParameterNode> parameters) {
        this(node, parameters, node.getRange());
    }

    public BoundParameterListNode(ParameterListNode node, List<BoundParameterNode> parameters, TextRange range) {
        super(BoundNodeType.PARAMETER_LIST, range);
        this.syntaxNode = node;
        this.parameters = parameters;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundParameterNode parameter : parameters) {
            parameter.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(parameters);
    }
}