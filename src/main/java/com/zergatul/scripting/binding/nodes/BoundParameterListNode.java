package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class BoundParameterListNode extends BoundNode {

    public final List<BoundParameterNode> parameters;

    public BoundParameterListNode(List<BoundParameterNode> parameters, TextRange range) {
        super(NodeType.PARAMETER_LIST, range);
        this.parameters = parameters;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundParameterListNode other) {
            return Objects.equals(other.parameters, parameters) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}