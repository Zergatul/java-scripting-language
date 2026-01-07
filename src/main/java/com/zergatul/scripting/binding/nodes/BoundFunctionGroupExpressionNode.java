package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.type.SMethodGroup;

import java.util.List;

public class BoundFunctionGroupExpressionNode extends BoundExpressionNode {

    public final NameExpressionNode syntaxNode;
    public final List<Function> candidates;

    public BoundFunctionGroupExpressionNode(NameExpressionNode node, List<Function> candidates) {
        super(BoundNodeType.FUNCTION_GROUP, new SMethodGroup(), node.getRange());
        this.syntaxNode = node;
        this.candidates = candidates;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}